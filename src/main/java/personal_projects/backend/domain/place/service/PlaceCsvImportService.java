package personal_projects.backend.domain.place.service;

import com.opencsv.CSVReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import personal_projects.backend.domain.place.config.PlaceCsvProperties;
import personal_projects.backend.domain.place.dto.response.PlaceCsvImportResponse;
import personal_projects.backend.domain.place.entity.PlaceCategory;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceCsvImportService {

    private static final String UPSERT_SQL = """
        INSERT INTO place
            (name, category, facility_type, address, tel, active, coordinate)
        VALUES
            (?, ?, ?, ?, ?, true, ST_GeomFromText(?, 4326, 'axis-order=long-lat'))
        ON DUPLICATE KEY UPDATE
            category = VALUES(category),
            facility_type = VALUES(facility_type),
            tel = VALUES(tel),
            active = true,
            coordinate = VALUES(coordinate)
        """;

    private final JdbcTemplate jdbcTemplate;
    private final PlaceCsvProperties properties;

    @Transactional
    public PlaceCsvImportResponse importAll() {
        jdbcTemplate.update("UPDATE place SET active = false");

        ImportCount hospitalCount = importFile(
            properties.hospitalPath(), PlaceCategory.HOSPITAL,
            1, 3, 10, 11, 28, 29
        );
        ImportCount pharmacyCount = importFile(
            properties.pharmacyPath(), PlaceCategory.PHARMACY,
            1, 3, 10, 11, 13, 14
        );
        int deleted = jdbcTemplate.update("DELETE FROM place WHERE active = false");
        ensureSpatialIndex();

        return new PlaceCsvImportResponse(
            hospitalCount.imported(),
            pharmacyCount.imported(),
            hospitalCount.skipped() + pharmacyCount.skipped(),
            deleted
        );
    }

    private ImportCount importFile(
        String path,
        PlaceCategory category,
        int nameIndex,
        int facilityTypeIndex,
        int addressIndex,
        int phoneIndex,
        int longitudeIndex,
        int latitudeIndex
    ) {
        int imported = 0;
        int skipped = 0;
        List<PlaceCsvRow> batch = new ArrayList<>(properties.batchSize());

        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream inputStream = resource.getInputStream();
             InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             CSVReader csvReader = new CSVReader(reader)) {
            csvReader.readNext();
            String[] row;
            while ((row = csvReader.readNext()) != null) {
                try {
                    PlaceCsvRow parsed = parse(
                        row, category, nameIndex, facilityTypeIndex, addressIndex,
                        phoneIndex, longitudeIndex, latitudeIndex
                    );
                    batch.add(parsed);
                    imported++;
                    if (batch.size() >= properties.batchSize()) {
                        saveBatch(batch);
                        batch.clear();
                    }
                } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException exception) {
                    skipped++;
                }
            }
            saveBatch(batch);
        } catch (Exception exception) {
            throw new IllegalStateException("CSV 파일을 읽지 못했습니다: " + path, exception);
        }

        log.info("장소 CSV 반영 완료: category={}, imported={}, skipped={}", category, imported, skipped);
        return new ImportCount(imported, skipped);
    }

    private PlaceCsvRow parse(
        String[] row,
        PlaceCategory category,
        int nameIndex,
        int facilityTypeIndex,
        int addressIndex,
        int phoneIndex,
        int longitudeIndex,
        int latitudeIndex
    ) {
        String name = row[nameIndex].trim();
        String address = row[addressIndex].trim();
        String facilityType = row[facilityTypeIndex].trim();
        String phoneNumber = row[phoneIndex].trim();
        double longitude = Double.parseDouble(row[longitudeIndex]);
        double latitude = Double.parseDouble(row[latitudeIndex]);
        if (name.isBlank() || address.isBlank()
            || longitude < -180 || longitude > 180
            || latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("유효하지 않은 장소 데이터");
        }
        return new PlaceCsvRow(
            name, category, facilityType, address, phoneNumber, longitude, latitude
        );
    }

    private void saveBatch(List<PlaceCsvRow> batch) {
        if (batch.isEmpty()) {
            return;
        }
        jdbcTemplate.batchUpdate(UPSERT_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement statement, int index) throws java.sql.SQLException {
                PlaceCsvRow row = batch.get(index);
                statement.setString(1, row.name());
                statement.setString(2, row.category().name());
                statement.setString(3, row.facilityType());
                statement.setString(4, row.address());
                statement.setString(5, row.phoneNumber());
                statement.setString(6, "POINT(%f %f)".formatted(row.longitude(), row.latitude()));
            }

            @Override
            public int getBatchSize() {
                return batch.size();
            }
        });
    }

    private void ensureSpatialIndex() {
        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'place'
              AND index_name = 'idx_place_coordinate'
            """, Integer.class);
        if (count != null && count == 0) {
            jdbcTemplate.execute("""
                ALTER TABLE place
                MODIFY coordinate POINT SRID 4326 NOT NULL,
                ADD SPATIAL INDEX idx_place_coordinate (coordinate)
                """);
        }
    }

    private record PlaceCsvRow(
        String name,
        PlaceCategory category,
        String facilityType,
        String address,
        String phoneNumber,
        double longitude,
        double latitude
    ) {
    }

    private record ImportCount(int imported, int skipped) {
    }
}
