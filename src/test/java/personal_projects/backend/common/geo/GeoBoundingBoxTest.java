package personal_projects.backend.common.geo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static personal_projects.backend.common.geo.GeoBoundingBox.MYSQL_SPHERE_RADIUS_METERS;

import java.util.Locale;
import java.util.Random;
import org.junit.jupiter.api.Test;

class GeoBoundingBoxTest {

    private static final double[][] CENTERS = {
        {127.1112, 37.3948},   // 판교역
        {126.9780, 37.5665},   // 서울시청
        {129.0756, 35.1796},   // 부산
        {126.5312, 33.4996},   // 제주
        {10.0, 60.0},          // 고위도
        {0.0, 0.0}             // 적도
    };
    private static final double[] RADII_METERS = {1_000, 3_000, 5_000, 10_000, 30_000, 50_000};

    @Test
    void 반경_경계_바로_안쪽의_점은_모든_방향에서_사각형에_포함된다() {
        for (double[] center : CENTERS) {
            for (double radius : RADII_METERS) {
                GeoBoundingBox box = GeoBoundingBox.around(center[0], center[1], radius);
                for (int bearing = 0; bearing < 360; bearing++) {
                    double[] point = destination(center[0], center[1], bearing, radius * 0.99999);
                    assertTrue(box.contains(point[0], point[1]),
                        "center=%s,%s radius=%s bearing=%d".formatted(center[0], center[1], radius, bearing));
                }
            }
        }
    }

    @Test
    void 반경_안의_무작위_점은_모두_사각형에_포함된다() {
        Random random = new Random(42);
        for (double[] center : CENTERS) {
            for (double radius : RADII_METERS) {
                GeoBoundingBox box = GeoBoundingBox.around(center[0], center[1], radius);
                double spread = Math.toDegrees(radius / MYSQL_SPHERE_RADIUS_METERS) * 3;
                for (int i = 0; i < 2_000; i++) {
                    double longitude = center[0] + (random.nextDouble() * 2 - 1) * spread
                        / Math.cos(Math.toRadians(center[1]));
                    double latitude = center[1] + (random.nextDouble() * 2 - 1) * spread;
                    if (distanceSphere(center[0], center[1], longitude, latitude) <= radius) {
                        assertTrue(box.contains(longitude, latitude));
                    }
                }
            }
        }
    }

    @Test
    void 판교_30km_정북_경계_점을_놓치지_않는다() {
        double[] north = destination(127.1112, 37.3948, 0, 29_990);
        double[] east = destination(127.1112, 37.3948, 90, 29_990);

        GeoBoundingBox box = GeoBoundingBox.aroundKilometers(127.1112, 37.3948, 30);

        assertTrue(box.contains(north[0], north[1]));
        assertTrue(box.contains(east[0], east[1]));
        // 기존 r / 111.32 방식은 북쪽 경계가 약 29.966km라 이 점을 놓쳤다.
        assertTrue(north[1] > 37.3948 + 30 / 111.32);
    }

    @Test
    void 사각형_여유는_반경의_0_1퍼센트를_넘지_않는다() {
        GeoBoundingBox box = GeoBoundingBox.aroundKilometers(127.1112, 37.3948, 30);

        double northEdge = distanceSphere(127.1112, 37.3948, 127.1112, box.maxLatitude());

        assertTrue(northEdge >= 30_000);
        assertTrue(northEdge <= 30_000 * (1 + GeoBoundingBox.MARGIN_RATIO) + 0.01);
    }

    @Test
    void WKT는_기본_로케일과_상관없이_소수점을_쓴다() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);
            String wkt = GeoBoundingBox.aroundKilometers(127.1112, 37.3948, 3).toWktPolygon();

            assertTrue(wkt.startsWith("POLYGON((127."));
            assertFalse(wkt.matches(".*\\d,\\d.*"));
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    void 반경은_0보다_커야_한다() {
        assertThrows(IllegalArgumentException.class, () -> GeoBoundingBox.around(127, 37, 0));
    }

    static double distanceSphere(double lon1, double lat1, double lon2, double lat2) {
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double dPhi = phi2 - phi1;
        double dLambda = Math.toRadians(lon2 - lon1);
        double a = Math.pow(Math.sin(dPhi / 2), 2)
            + Math.cos(phi1) * Math.cos(phi2) * Math.pow(Math.sin(dLambda / 2), 2);
        return 2 * MYSQL_SPHERE_RADIUS_METERS * Math.asin(Math.min(1, Math.sqrt(a)));
    }

    static double[] destination(double longitude, double latitude, double bearingDegrees, double meters) {
        double delta = meters / MYSQL_SPHERE_RADIUS_METERS;
        double theta = Math.toRadians(bearingDegrees);
        double phi1 = Math.toRadians(latitude);
        double lambda1 = Math.toRadians(longitude);
        double phi2 = Math.asin(Math.sin(phi1) * Math.cos(delta)
            + Math.cos(phi1) * Math.sin(delta) * Math.cos(theta));
        double lambda2 = lambda1 + Math.atan2(
            Math.sin(theta) * Math.sin(delta) * Math.cos(phi1),
            Math.cos(delta) - Math.sin(phi1) * Math.sin(phi2)
        );
        return new double[]{Math.toDegrees(lambda2), Math.toDegrees(phi2)};
    }
}
