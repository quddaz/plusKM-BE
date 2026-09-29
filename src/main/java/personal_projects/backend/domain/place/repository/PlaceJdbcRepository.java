package personal_projects.backend.domain.place.repository;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import personal_projects.backend.domain.place.dto.request.NearbyPlaceSearchRequest;
import personal_projects.backend.domain.place.dto.response.NearbyPlaceResponse;
import personal_projects.backend.domain.place.entity.PlaceCategory;

@Repository
@RequiredArgsConstructor
public class PlaceJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<NearbyPlaceResponse> findNearby(NearbyPlaceSearchRequest request) {
        double latitudeDistance = request.radiusKilometers() / 111.32;
        double longitudeDistance = request.radiusKilometers()
            / (111.32 * Math.cos(Math.toRadians(request.latitude())));
        String polygon = createPolygon(
            request.longitude(),
            request.latitude(),
            longitudeDistance,
            latitudeDistance
        );

        String categoryCondition = request.category() == PlaceCategory.ALL
            ? ""
            : " AND category = ?";
        String sql = """
            SELECT id, name, category, facility_type, address, tel,
                   ST_X(coordinate) AS longitude,
                   ST_Y(coordinate) AS latitude,
                   ST_Distance_Sphere(
                       coordinate,
                       ST_SRID(POINT(?, ?), 4326)
                   ) AS distance_meters
            FROM place
            WHERE active = true
              AND MBRContains(
                    ST_GeomFromText(?, 4326, 'axis-order=long-lat'),
                    coordinate
                  )
            """ + categoryCondition + """
            HAVING distance_meters <= ?
            ORDER BY distance_meters
            LIMIT 200
            """;

        Object[] parameters = request.category() == PlaceCategory.ALL
            ? new Object[]{request.longitude(), request.latitude(), polygon,
                request.radiusKilometers() * 1_000}
            : new Object[]{request.longitude(), request.latitude(), polygon,
                request.category().name(), request.radiusKilometers() * 1_000};

        return jdbcTemplate.query(sql, (resultSet, rowNumber) -> new NearbyPlaceResponse(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            PlaceCategory.valueOf(resultSet.getString("category")),
            resultSet.getString("facility_type"),
            resultSet.getString("address"),
            resultSet.getString("tel"),
            resultSet.getDouble("longitude"),
            resultSet.getDouble("latitude"),
            resultSet.getDouble("distance_meters")
        ), parameters);
    }

    private String createPolygon(
        double longitude,
        double latitude,
        double longitudeDistance,
        double latitudeDistance
    ) {
        double longitudeMin = longitude - longitudeDistance;
        double longitudeMax = longitude + longitudeDistance;
        double latitudeMin = latitude - latitudeDistance;
        double latitudeMax = latitude + latitudeDistance;

        return "POLYGON((%f %f, %f %f, %f %f, %f %f, %f %f))".formatted(
            longitudeMin, latitudeMin,
            longitudeMin, latitudeMax,
            longitudeMax, latitudeMax,
            longitudeMax, latitudeMin,
            longitudeMin, latitudeMin
        );
    }
}
