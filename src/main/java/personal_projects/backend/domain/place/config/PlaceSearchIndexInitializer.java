package personal_projects.backend.domain.place.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class PlaceSearchIndexInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments arguments) {
        // SRID 4326의 첫 축은 위도이므로 ST_X는 위도, ST_Y는 경도를 반환한다.
        if (!hasColumn("longitude") && !hasColumn("latitude")) {
            jdbcTemplate.execute("""
                ALTER TABLE place
                ADD COLUMN longitude DOUBLE
                    GENERATED ALWAYS AS (ST_Y(coordinate)) STORED,
                ADD COLUMN latitude DOUBLE
                    GENERATED ALWAYS AS (ST_X(coordinate)) STORED,
                ADD INDEX idx_place_lat_lon (latitude, longitude),
                ADD INDEX idx_place_lon_lat (longitude, latitude)
                """);
            return;
        }
        ensureCoordinateColumn("longitude", "ST_Y(coordinate)");
        ensureCoordinateColumn("latitude", "ST_X(coordinate)");
        boolean missingLatitudeIndex = !hasIndex("idx_place_lat_lon");
        boolean missingLongitudeIndex = !hasIndex("idx_place_lon_lat");
        if (missingLatitudeIndex && missingLongitudeIndex) {
            jdbcTemplate.execute("""
                ALTER TABLE place
                ADD INDEX idx_place_lat_lon (latitude, longitude),
                ADD INDEX idx_place_lon_lat (longitude, latitude)
                """);
        } else if (missingLatitudeIndex) {
            jdbcTemplate.execute("CREATE INDEX idx_place_lat_lon ON place (latitude, longitude)");
        } else if (missingLongitudeIndex) {
            jdbcTemplate.execute("CREATE INDEX idx_place_lon_lat ON place (longitude, latitude)");
        }
    }

    private boolean hasColumn(String columnName) {
        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'place'
              AND column_name = ?
            """, Integer.class, columnName);
        return count != null && count > 0;
    }

    private void ensureCoordinateColumn(String columnName, String expression) {
        List<String> columnTypes = jdbcTemplate.queryForList("""
            SELECT extra
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'place'
              AND column_name = ?
            """, String.class, columnName);
        if (columnTypes.isEmpty()) {
            jdbcTemplate.execute("ALTER TABLE place ADD COLUMN " + columnName
                + " DOUBLE GENERATED ALWAYS AS (" + expression + ") STORED");
        } else if (columnTypes.getFirst().isBlank()) {
            jdbcTemplate.execute("ALTER TABLE place MODIFY COLUMN " + columnName
                + " DOUBLE GENERATED ALWAYS AS (" + expression + ") STORED");
        } else if (!columnTypes.getFirst().contains("STORED GENERATED")) {
            throw new IllegalStateException(columnName + " 열은 저장형 생성 열이어야 합니다.");
        }
    }

    private boolean hasIndex(String indexName) {
        Integer count = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'place'
              AND index_name = ?
            """, Integer.class, indexName);
        return count != null && count > 0;
    }
}
