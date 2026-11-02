package personal_projects.backend.domain.place.service;

import com.opencsv.CSVReader;
import java.io.InputStreamReader;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import personal_projects.backend.domain.place.config.PlaceCsvProperties;
import personal_projects.backend.domain.place.importer.PlaceCsvReader;
import personal_projects.backend.domain.place.repository.PlaceCsvStageRepository;

/** 로컬 MySQL에서 명시적으로 실행하는 CSV 갱신 비교 실험. 일반 테스트에서는 건너뛴다. */
class PlaceCsvImportBenchmarkTest {

    private static final int BATCH_SIZE = 1_000;

    @Test
    @Timeout(value = 20, unit = TimeUnit.MINUTES)
    void compareCurrentImporterWithStagingDiff() throws Exception {
        String port = System.getenv("PLUSKM_BENCH_DB_PORT");
        Assumptions.assumeTrue(port != null, "로컬 CSV 벤치마크 요청이 있을 때만 실행");
        String password = System.getenv("PLUSKM_BENCH_DB_PASSWORD");
        Assumptions.assumeTrue(password != null, "실험용 MySQL 암호가 필요함");

        DriverManagerDataSource dataSource = new DriverManagerDataSource(
            "jdbc:mysql://127.0.0.1:" + port + "/pluskm_benchmark?rewriteBatchedStatements=true",
            "root", password
        );
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        TransactionTemplate transaction = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
        createTables(jdbc);

        PlaceCsvImportService importer = new PlaceCsvImportService(
            new PlaceCsvStageRepository(jdbc),
            new PlaceCsvReader(),
            new PlaceCsvProperties("data/병원정보.csv", "data/약국정보.csv", BATCH_SIZE, false)
        );
        // 두 비교 모두 동일한 기존 데이터가 있는 '재갱신' 상황에서 측정한다.
        transaction.executeWithoutResult(status -> importer.importAll());
        long initialCount = jdbc.queryForObject("SELECT COUNT(*) FROM place", Long.class);
        Result currentA = measure("현재-A", () ->
            transaction.executeWithoutResult(status -> importer.importAll()));
        Result stagingA = measure("스테이징-A", () -> stageAndMerge(jdbc, transaction));
        Result stagingB = measure("스테이징-B", () -> stageAndMerge(jdbc, transaction));
        Result currentB = measure("현재-B", () ->
            transaction.executeWithoutResult(status -> importer.importAll()));
        long finalCount = jdbc.queryForObject("SELECT COUNT(*) FROM place", Long.class);

        System.out.printf("CSV_BENCH rows=%d finalRows=%d%n", initialCount, finalCount);
        System.out.println(currentA);
        System.out.println(stagingA);
        System.out.println(stagingB);
        System.out.println(currentB);
        org.junit.jupiter.api.Assertions.assertEquals(initialCount, finalCount);
    }

    private void createTables(JdbcTemplate jdbc) {
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS place (
              id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
              name VARCHAR(255) NOT NULL,
              category VARCHAR(20) NOT NULL,
              facility_type VARCHAR(40) NOT NULL,
              address VARCHAR(255) NOT NULL,
              tel VARCHAR(40),
              active BOOLEAN NOT NULL,
              coordinate POINT SRID 4326 NOT NULL,
              UNIQUE KEY uk_place_name_address (name, address),
              SPATIAL INDEX idx_place_coordinate (coordinate)
            ) ENGINE=InnoDB
            """);
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS place_csv_stage_exact (
              name VARCHAR(255) NOT NULL,
              category VARCHAR(20) NOT NULL,
              facility_type VARCHAR(40) NOT NULL,
              address VARCHAR(255) NOT NULL,
              tel VARCHAR(40),
              coordinate POINT SRID 4326 NOT NULL,
              UNIQUE KEY uk_place_stage_name_address (name, address)
            ) ENGINE=InnoDB
            """);
    }

    private void stageAndMerge(JdbcTemplate jdbc, TransactionTemplate transaction) {
        jdbc.execute("TRUNCATE TABLE place_csv_stage_exact");
        readIntoStage(jdbc, "data/병원정보.csv", "HOSPITAL", 28, 29);
        readIntoStage(jdbc, "data/약국정보.csv", "PHARMACY", 13, 14);

        long staged = jdbc.queryForObject("SELECT COUNT(*) FROM place_csv_stage_exact", Long.class);
        long existing = jdbc.queryForObject("SELECT COUNT(*) FROM place", Long.class);
        if (staged < existing / 2 || staged == 0) {
            throw new IllegalStateException("스테이징 데이터가 비정상적으로 적어 반영을 중단함: " + staged);
        }
        transaction.executeWithoutResult(status -> {
            int updated = jdbc.update("""
                UPDATE place p JOIN place_csv_stage_exact s ON p.name = s.name AND p.address = s.address
                SET p.category = s.category, p.facility_type = s.facility_type,
                    p.tel = s.tel, p.active = true,
                    p.coordinate = s.coordinate
                WHERE NOT (p.category <=> s.category AND p.facility_type <=> s.facility_type
                    AND p.tel <=> s.tel AND p.active = true
                    AND ST_Equals(p.coordinate, s.coordinate))
                """);
            int inserted = jdbc.update("""
                INSERT INTO place (name, category, facility_type, address, tel, active, coordinate)
                SELECT s.name, s.category, s.facility_type, s.address, s.tel, true, s.coordinate
                FROM place_csv_stage_exact s
                LEFT JOIN place p ON p.name = s.name AND p.address = s.address
                WHERE p.id IS NULL
                """);
            int deleted = jdbc.update("""
                DELETE p FROM place p
                LEFT JOIN place_csv_stage_exact s ON p.name = s.name AND p.address = s.address
                WHERE s.name IS NULL
                """);
            System.out.printf("CSV_BENCH staging_rows=%d updated=%d inserted=%d deleted=%d%n",
                staged, updated, inserted, deleted);
        });
    }

    private void readIntoStage(JdbcTemplate jdbc, String path, String category,
                               int longitudeIndex, int latitudeIndex) {
        List<Row> batch = new ArrayList<>(BATCH_SIZE);
        try (CSVReader reader = new CSVReader(new InputStreamReader(
                new ClassPathResource(path).getInputStream(), StandardCharsets.UTF_8))) {
            reader.readNext();
            String[] values;
            while ((values = reader.readNext()) != null) {
                try {
                    String name = values[1].trim();
                    String address = values[10].trim();
                    double longitude = Double.parseDouble(values[longitudeIndex]);
                    double latitude = Double.parseDouble(values[latitudeIndex]);
                    if (name.isBlank() || address.isBlank() || Math.abs(longitude) > 180
                        || Math.abs(latitude) > 90) continue;
                    batch.add(new Row(name, category, values[3].trim(), address,
                        values[11].trim(), "POINT(%f %f)".formatted(longitude, latitude)));
                    if (batch.size() >= BATCH_SIZE) {
                        insertStageBatch(jdbc, batch);
                        batch.clear();
                    }
                } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException ignored) {
                    // 운영 코드와 동일하게 유효하지 않은 CSV 행은 건너뛴다.
                }
            }
            insertStageBatch(jdbc, batch);
        } catch (Exception exception) {
            throw new IllegalStateException("스테이징 CSV 읽기 실패: " + path, exception);
        }
    }

    private void insertStageBatch(JdbcTemplate jdbc, List<Row> batch) {
        if (batch.isEmpty()) return;
        jdbc.batchUpdate("""
            INSERT INTO place_csv_stage_exact
              (name, category, facility_type, address, tel, coordinate)
            VALUES (?, ?, ?, ?, ?, ST_GeomFromText(?, 4326, 'axis-order=long-lat'))
            ON DUPLICATE KEY UPDATE category=VALUES(category), facility_type=VALUES(facility_type),
              tel=VALUES(tel), coordinate=VALUES(coordinate)
            """, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(java.sql.PreparedStatement ps, int index) throws java.sql.SQLException {
                    Row row = batch.get(index);
                    ps.setString(1, row.name());
                    ps.setString(2, row.category());
                    ps.setString(3, row.facilityType());
                    ps.setString(4, row.address());
                    ps.setString(5, row.tel());
                    ps.setString(6, row.wkt());
                }
                @Override public int getBatchSize() { return batch.size(); }
            });
    }

    private Result measure(String label, Runnable work) throws InterruptedException {
        System.gc();
        Thread.sleep(200);
        AtomicBoolean running = new AtomicBoolean(true);
        AtomicLong peakHeap = new AtomicLong();
        Thread sampler = new Thread(() -> {
            while (running.get()) {
                long used = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
                peakHeap.accumulateAndGet(used, Math::max);
                try { Thread.sleep(10); } catch (InterruptedException ignored) { break; }
            }
        }, "csv-benchmark-heap-sampler");
        long gcBefore = gcMillis();
        long start = System.nanoTime();
        sampler.start();
        try {
            work.run();
        } finally {
            running.set(false);
            sampler.join();
        }
        return new Result(label, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start),
            peakHeap.get() / (1024 * 1024), gcMillis() - gcBefore);
    }

    private long gcMillis() {
        return ManagementFactory.getGarbageCollectorMXBeans().stream()
            .mapToLong(GarbageCollectorMXBean::getCollectionTime).sum();
    }

    private record Row(String name, String category, String facilityType, String address,
                       String tel, String wkt) {}
    private record Result(String label, long elapsedMs, long peakHeapMiB, long gcMs) {}
}
