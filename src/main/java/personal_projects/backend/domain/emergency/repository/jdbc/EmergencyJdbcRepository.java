package personal_projects.backend.domain.emergency.repository.jdbc;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import personal_projects.backend.domain.emergency.entity.Emergency;
import personal_projects.backend.domain.emergency.type.EmergencySearchType;
import personal_projects.backend.domain.emergency.dto.response.EmergencyDetailResponse;
import personal_projects.backend.domain.emergency.dto.response.NearbyEmergencyResponse;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class EmergencyJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<NearbyEmergencyResponse> findNearbyEmergencies(
        double longitude,
        double latitude,
        double radiusKilometers,
        EmergencySearchType searchType
    ) {
        double latitudeDistance = radiusKilometers / 111.32;
        double longitudeDistance = radiusKilometers / (111.32 * Math.cos(Math.toRadians(latitude)));
        String polygon = createPolygon(longitude, latitude, longitudeDistance, latitudeDistance);

        StringBuilder sql = new StringBuilder("""
            SELECT id, name, address, tel AS phone_number,
                   ST_X(coordinate) AS longitude, ST_Y(coordinate) AS latitude
            FROM emergency
            WHERE active = true
              AND ST_Within(coordinate, ST_GeomFromText(?, 4326, 'axis-order=long-lat'))
            """);
        List<Object> parameters = new ArrayList<>();
        parameters.add(polygon);

        if (searchType == EmergencySearchType.HOSPITAL) {
            sql.append(" AND emergency_type <> ?");
            parameters.add("약국");
        } else if (searchType == EmergencySearchType.PHARMACY) {
            sql.append(" AND emergency_type = ?");
            parameters.add("약국");
        }

        return jdbcTemplate.query(sql.toString(), (resultSet, rowNumber) ->
            new NearbyEmergencyResponse(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("address"),
                resultSet.getString("phone_number"),
                resultSet.getDouble("longitude"),
                resultSet.getDouble("latitude")
            ), parameters.toArray());
    }

    public EmergencyDetailResponse findEmergencyDetail(Long emergencyId) {
        String sql = """
            SELECT p.id, p.name, p.address, p.tel AS phone_number, p.emergency_type
            FROM emergency p
            WHERE p.id = ?
            """;

        return jdbcTemplate.query(sql, resultSet -> {
            if (!resultSet.next()) {
                return null;
            }
            return new EmergencyDetailResponse(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("address"),
                resultSet.getString("phone_number"),
                resultSet.getString("emergency_type")
            );
        }, emergencyId);
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
