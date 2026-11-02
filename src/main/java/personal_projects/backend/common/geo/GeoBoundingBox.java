package personal_projects.backend.common.geo;

import java.util.Locale;

public record GeoBoundingBox(
    double minLongitude,
    double minLatitude,
    double maxLongitude,
    double maxLatitude
) {

    public static final double MYSQL_SPHERE_RADIUS_METERS = 6_370_986.0;

    static final double MARGIN_RATIO = 0.001;

    public static GeoBoundingBox around(double longitude, double latitude, double radiusMeters) {
        if (radiusMeters <= 0) {
            throw new IllegalArgumentException("반경은 0보다 커야 합니다: " + radiusMeters);
        }
        double angularRadius = radiusMeters * (1 + MARGIN_RATIO) / MYSQL_SPHERE_RADIUS_METERS;
        double latitudeDelta = Math.toDegrees(angularRadius);
        double cosLatitude = Math.cos(Math.toRadians(latitude));
        double longitudeDelta = Math.sin(angularRadius) >= cosLatitude
            ? 180
            : Math.toDegrees(Math.asin(Math.sin(angularRadius) / cosLatitude));

        return new GeoBoundingBox(
            Math.max(-180, longitude - longitudeDelta),
            Math.max(-90, latitude - latitudeDelta),
            Math.min(180, longitude + longitudeDelta),
            Math.min(90, latitude + latitudeDelta)
        );
    }

    public static GeoBoundingBox aroundKilometers(double longitude, double latitude, double radiusKilometers) {
        return around(longitude, latitude, radiusKilometers * 1_000);
    }

    public boolean contains(double longitude, double latitude) {
        return minLongitude <= longitude && longitude <= maxLongitude
            && minLatitude <= latitude && latitude <= maxLatitude;
    }

    public String toWktPolygon() {
        return String.format(Locale.ROOT,
            "POLYGON((%.7f %.7f, %.7f %.7f, %.7f %.7f, %.7f %.7f, %.7f %.7f))",
            minLongitude, minLatitude,
            minLongitude, maxLatitude,
            maxLongitude, maxLatitude,
            maxLongitude, minLatitude,
            minLongitude, minLatitude
        );
    }
}
