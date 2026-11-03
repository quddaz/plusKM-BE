package personal_projects.backend.domain.place.repository;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import personal_projects.backend.common.geo.GeoBoundingBox;
import personal_projects.backend.domain.place.dto.request.NearbyPlaceSearchRequest;
import personal_projects.backend.domain.place.dto.response.NearbyPlaceResponse;
import personal_projects.backend.domain.place.entity.PlaceCategory;

@Repository
@RequiredArgsConstructor
public class PlaceJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<NearbyPlaceResponse> findNearby(NearbyPlaceSearchRequest request) {
        GeoBoundingBox bounds = GeoBoundingBox.aroundKilometers(
            request.longitude(), request.latitude(), request.radiusKilometers()
        );

        String categoryCondition = request.category() == PlaceCategory.ALL
            ? ""
            : " AND category = ?";
        String sql = """
            SELECT id, name, category, facility_type, address, tel,
                   longitude, latitude,
                   2 * 6370986 * ASIN(LEAST(1, SQRT(
                       POWER(SIN(RADIANS(latitude - ?) / 2), 2)
                       + COS(RADIANS(?)) * COS(RADIANS(latitude))
                       * POWER(SIN(RADIANS(longitude - ?) / 2), 2)
                   ))) AS distance_meters
            FROM place
            WHERE active = true
              AND latitude BETWEEN ? AND ?
              AND longitude BETWEEN ? AND ?
            """ + categoryCondition + """
            HAVING distance_meters <= ?
            ORDER BY distance_meters, id
            """;

        List<Object> parameters = new ArrayList<>(List.of(
            request.latitude(), request.latitude(), request.longitude(),
            bounds.minLatitude(), bounds.maxLatitude(),
            bounds.minLongitude(), bounds.maxLongitude()
        ));
        if (request.category() != PlaceCategory.ALL) {
            parameters.add(request.category().name());
        }
        parameters.add(request.radiusKilometers() * 1_000);

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
        ), parameters.toArray());
    }
}
