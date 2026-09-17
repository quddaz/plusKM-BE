package personal_projects.backend.domain.emergency.repository.jdbc;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import personal_projects.backend.domain.emergency.entity.Emergency;
import personal_projects.backend.domain.emergency.dto.response.EmergencyDetailResponse;
import personal_projects.backend.domain.emergency.dto.response.NearbyEmergencyResponse;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class EmergencyJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<NearbyEmergencyResponse> findNearbyEmergencies(
        double longitude,
        double latitude,
        double radiusKilometers
    ) {
        double latitudeDistance = radiusKilometers / 111.32;
        double longitudeDistance = radiusKilometers / (111.32 * Math.cos(Math.toRadians(latitude)));
        String polygon = createPolygon(longitude, latitude, longitudeDistance, latitudeDistance);

        String sql = """
            SELECT id, hpid, name, address, tel AS phone_number,
                   ST_X(coordinate) AS longitude, ST_Y(coordinate) AS latitude
            FROM emergency
            WHERE active = true
              AND ST_Within(coordinate, ST_GeomFromText(?, 4326, 'axis-order=long-lat'))
            """;

        return jdbcTemplate.query(sql, (resultSet, rowNumber) ->
            new NearbyEmergencyResponse(
                resultSet.getLong("id"),
                resultSet.getString("hpid"),
                resultSet.getString("name"),
                resultSet.getString("address"),
                resultSet.getString("phone_number"),
                resultSet.getDouble("longitude"),
                resultSet.getDouble("latitude")
            ), polygon);
    }

    public EmergencyDetailResponse findEmergencyDetail(Long emergencyId) {
        String sql = """
            SELECT p.id, p.name, p.address, p.tel AS phone_number
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
                resultSet.getString("phone_number")
            );
        }, emergencyId);
    }

    /*
     * 공간 데이터베이스 벤치마크 실험 기록
     *
     * 배포 중인 애플리케이션의 저장소 동작을 변경하지 않으면서 벤치마크에서 사용한
     * PostGIS, MySQL, MongoDB 조회 방식
     *
     * 공통 실험 조건
     * - 좌표 체계: WGS84 / SRID 4326, 좌표 순서: 경도, 위도
     * - 인덱스로 사각형 후보를 추린 뒤 정확한 구면 거리를 계산
     * - 거리 오름차순 정렬, LIMIT 50과 전체 반환을 각각 측정
     *
     * PostGIS / JDBC 실험 (GiST 인덱스)
     *
     * SELECT source_id,
     *        name,
     *        ST_DistanceSphere(
     *            location,
     *            ST_SetSRID(ST_MakePoint(?, ?), 4326)
     *        ) AS distance_meters
     * FROM facilities
     * WHERE location && ST_MakeEnvelope(?, ?, ?, ?, 4326)
     *   AND ST_DistanceSphere(
     *           location,
     *           ST_SetSRID(ST_MakePoint(?, ?), 4326)
     *       ) <= ?
     * ORDER BY distance_meters
     * LIMIT ?;
     *
     * MySQL / JDBC 실험 (SPATIAL 인덱스)
     *
     * SELECT source_id,
     *        name,
     *        ST_Distance_Sphere(
     *            location,
     *            ST_SRID(POINT(?, ?), 4326)
     *        ) AS distance_meters
     * FROM facilities FORCE INDEX (facilities_location_six)
     * WHERE MBRContains(
     *           ST_GeomFromText(?, 4326, 'axis-order=long-lat'),
     *           location
     *       )
     *   AND ST_Distance_Sphere(
     *           location,
     *           ST_SRID(POINT(?, ?), 4326)
     *       ) <= ?
     * ORDER BY distance_meters
     * LIMIT ?;
     *
     * MongoDB 실험 (JDBC 방식이 아니며 2dsphere 인덱스와 $geoNear 사용)
     *
     * db.facilities.aggregate([
     *   {
     *     $geoNear: {
     *       near: { type: "Point", coordinates: [longitude, latitude] },
     *       distanceField: "distanceMeters",
     *       maxDistance: radiusMeters,
     *       spherical: true
     *     }
     *   },
     *   { $limit: 50 },
     *   { $project: { name: 1, distanceMeters: 1 } }
     * ]);
     */

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
