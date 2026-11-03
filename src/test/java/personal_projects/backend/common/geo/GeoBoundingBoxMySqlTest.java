package personal_projects.backend.common.geo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import personal_projects.backend.support.MySqlIntegrationTest;

@Tag("repository")
class GeoBoundingBoxMySqlTest extends MySqlIntegrationTest {

    private static final double[][] CENTERS = {
        {127.1112, 37.3948}, {126.9780, 37.5665}, {129.0756, 35.1796}, {126.5312, 33.4996}
    };
    private static final double[] RADII_METERS = {1_000, 3_000, 10_000, 30_000};
    private static final double[] RADIUS_RATIOS = {0.995, 0.9999, 1.0001, 1.005};

    @Test
    void 사각형_후보와_구면_거리_결과가_전체_구면_거리_결과와_같다() {
        jdbc.execute("DROP TABLE IF EXISTS geo_bbox_check");
        jdbc.execute("""
            CREATE TABLE geo_bbox_check (
              id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
              coordinate POINT SRID 4326 NOT NULL,
              SPATIAL INDEX idx_geo_bbox_check_coordinate (coordinate)
            ) ENGINE=InnoDB
            """);
        try {
            insertPoints(jdbc);
            for (double[] center : CENTERS) {
                for (double radius : RADII_METERS) {
                    Set<Long> expected = fullScan(jdbc, center, radius);
                    Set<Long> actual = boxedScan(jdbc, center, radius);
                    assertFalse(expected.isEmpty());
                    assertEquals(expected, actual,
                        "center=%s,%s radius=%s".formatted(center[0], center[1], radius));
                }
            }
        } finally {
            jdbc.execute("DROP TABLE IF EXISTS geo_bbox_check");
        }
    }

    private void insertPoints(JdbcTemplate jdbc) {
        List<Object[]> points = new ArrayList<>();
        Random random = new Random(7);
        for (double[] center : CENTERS) {
            for (double radius : RADII_METERS) {
                for (int bearing = 0; bearing < 360; bearing += 5) {
                    for (double ratio : RADIUS_RATIOS) {
                        double[] point = GeoBoundingBoxTest.destination(
                            center[0], center[1], bearing, radius * ratio
                        );
                        points.add(new Object[]{point[0], point[1]});
                    }
                }
                for (int i = 0; i < 500; i++) {
                    double[] point = GeoBoundingBoxTest.destination(
                        center[0], center[1], random.nextDouble() * 360, random.nextDouble() * radius * 1.2
                    );
                    points.add(new Object[]{point[0], point[1]});
                }
            }
        }
        jdbc.batchUpdate(
            "INSERT INTO geo_bbox_check (coordinate) VALUES (ST_SRID(POINT(?, ?), 4326))", points
        );
    }

    private Set<Long> fullScan(JdbcTemplate jdbc, double[] center, double radius) {
        return new HashSet<>(jdbc.queryForList("""
            SELECT id FROM geo_bbox_check IGNORE INDEX (idx_geo_bbox_check_coordinate)
            WHERE ST_Distance_Sphere(coordinate, ST_SRID(POINT(?, ?), 4326)) <= ?
            """, Long.class, center[0], center[1], radius));
    }

    private Set<Long> boxedScan(JdbcTemplate jdbc, double[] center, double radius) {
        String polygon = GeoBoundingBox.around(center[0], center[1], radius).toWktPolygon();
        return new HashSet<>(jdbc.queryForList("""
            SELECT id FROM geo_bbox_check FORCE INDEX (idx_geo_bbox_check_coordinate)
            WHERE MBRContains(ST_GeomFromText(?, 4326, 'axis-order=long-lat'), coordinate)
              AND ST_Distance_Sphere(coordinate, ST_SRID(POINT(?, ?), 4326)) <= ?
            """, Long.class, polygon, center[0], center[1], radius));
    }
}
