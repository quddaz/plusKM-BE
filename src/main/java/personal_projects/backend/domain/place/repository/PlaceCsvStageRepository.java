package personal_projects.backend.domain.place.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import personal_projects.backend.domain.place.dto.PlaceCsvRow;

/**
 * CSV 반영용 임시 스테이징 테이블(place_csv_stage)과 운영 테이블(place) 병합 쿼리를 담당한다.
 * TEMPORARY TABLE은 커넥션 단위이므로 호출하는 쪽이 하나의 트랜잭션 안에서 사용해야 한다.
 */
@Repository
@RequiredArgsConstructor
public class PlaceCsvStageRepository {

    private static final String STAGE_UPSERT_SQL = """
        INSERT INTO place_csv_stage
            (name, category, facility_type, address, tel, coordinate)
        VALUES
            (?, ?, ?, ?, ?, ST_GeomFromText(?, 4326, 'axis-order=long-lat'))
        ON DUPLICATE KEY UPDATE
            category = VALUES(category),
            facility_type = VALUES(facility_type),
            tel = VALUES(tel),
            coordinate = VALUES(coordinate)
        """;

    private final JdbcTemplate jdbcTemplate;

    public void ensureSpatialIndex() {
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

    public void createStageTable() {
        jdbcTemplate.execute("""
            CREATE TEMPORARY TABLE place_csv_stage (
                name VARCHAR(255) NOT NULL,
                category VARCHAR(20) NOT NULL,
                facility_type VARCHAR(40) NOT NULL,
                address VARCHAR(255) NOT NULL,
                tel VARCHAR(40),
                coordinate POINT SRID 4326 NOT NULL,
                UNIQUE KEY uk_place_csv_stage_name_address (name, address)
            ) ENGINE=InnoDB
            """);
    }

    public void dropStageTable() {
        jdbcTemplate.execute("DROP TEMPORARY TABLE IF EXISTS place_csv_stage");
    }

    public void saveAllToStage(List<PlaceCsvRow> rows) {
        if (rows.isEmpty()) {
            return;
        }
        jdbcTemplate.batchUpdate(STAGE_UPSERT_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement statement, int index) throws SQLException {
                PlaceCsvRow row = rows.get(index);
                statement.setString(1, row.name());
                statement.setString(2, row.category().name());
                statement.setString(3, row.facilityType());
                statement.setString(4, row.address());
                statement.setString(5, row.phoneNumber());
                statement.setString(6, "POINT(%f %f)".formatted(row.longitude(), row.latitude()));
            }

            @Override
            public int getBatchSize() {
                return rows.size();
            }
        });
    }

    public int countStaged() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM place_csv_stage", Integer.class);
    }

    public int countActivePlaces() {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM place WHERE active = true", Integer.class
        );
    }

    public int countInactivePlaces() {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM place WHERE active = false", Integer.class
        );
    }

    /** 스테이징과 값이 달라진 기존 장소만 갱신한다. */
    public int updateChangedFromStage() {
        return jdbcTemplate.update("""
            UPDATE place p JOIN place_csv_stage s
              ON p.name = s.name AND p.address = s.address
            SET p.category = s.category, p.facility_type = s.facility_type,
                p.tel = s.tel, p.active = true, p.coordinate = s.coordinate
            WHERE NOT (p.category <=> s.category AND p.facility_type <=> s.facility_type
                AND p.tel <=> s.tel AND p.active = true
                AND ST_Equals(p.coordinate, s.coordinate))
            """);
    }

    /** 운영 테이블에 없는 스테이징 행을 추가한다. */
    public int insertNewFromStage() {
        return jdbcTemplate.update("""
            INSERT INTO place (name, category, facility_type, address, tel, active, coordinate)
            SELECT s.name, s.category, s.facility_type, s.address, s.tel, true, s.coordinate
            FROM place_csv_stage s
            LEFT JOIN place p ON p.name = s.name AND p.address = s.address
            WHERE p.id IS NULL
            """);
    }

    /** CSV에서 빠진 장소는 삭제하지 않고 비활성화한다. 기존 ID와 참조 관계를 보존한다. */
    public int deactivateMissingFromStage() {
        return jdbcTemplate.update("""
            UPDATE place p LEFT JOIN place_csv_stage s
              ON p.name = s.name AND p.address = s.address
            SET p.active = false
            WHERE s.name IS NULL AND p.active = true
            """);
    }
}
