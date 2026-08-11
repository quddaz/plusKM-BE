package personal_projects.backend.domain.place.repository.jdbc;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import personal_projects.backend.domain.place.entity.Place;
import personal_projects.backend.domain.place.type.PlaceSearchType;
import personal_projects.backend.domain.place.dto.response.BookmarkedPlaceResponse;
import personal_projects.backend.domain.place.dto.response.PlaceDetailResponse;
import personal_projects.backend.domain.place.dto.response.NearbyPlaceResponse;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PlaceJdbcRepository {

    private static final int BATCH_SIZE = 1000;

    private final JdbcTemplate jdbcTemplate;

    public List<NearbyPlaceResponse> findNearbyPlaces(
        double longitude,
        double latitude,
        double radiusKilometers,
        PlaceSearchType searchType
    ) {
        double latitudeDistance = radiusKilometers / 111.32;
        double longitudeDistance = radiusKilometers / (111.32 * Math.cos(Math.toRadians(latitude)));
        String polygon = createPolygon(longitude, latitude, longitudeDistance, latitudeDistance);

        StringBuilder sql = new StringBuilder("""
            SELECT id, name, address, tel AS phone_number,
                   ST_X(coordinate) AS longitude, ST_Y(coordinate) AS latitude
            FROM place
            WHERE active = true
              AND ST_Within(coordinate, ST_GeomFromText(?, 4326, 'axis-order=long-lat'))
            """);
        List<Object> parameters = new ArrayList<>();
        parameters.add(polygon);

        if (searchType == PlaceSearchType.HOSPITAL) {
            sql.append(" AND place_type <> ?");
            parameters.add("약국");
        } else if (searchType == PlaceSearchType.PHARMACY) {
            sql.append(" AND place_type = ?");
            parameters.add("약국");
        }

        return jdbcTemplate.query(sql.toString(), (resultSet, rowNumber) ->
            new NearbyPlaceResponse(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("address"),
                resultSet.getString("phone_number"),
                resultSet.getDouble("longitude"),
                resultSet.getDouble("latitude")
            ), parameters.toArray());
    }

    public PlaceDetailResponse findPlaceDetail(Long placeId, Long userId) {
        String sql = """
            SELECT p.id, p.name, p.address, p.tel AS phone_number, p.place_type,
                   EXISTS(
                       SELECT 1
                       FROM book_mark b
                       WHERE b.place_id = p.id AND b.user_id = ?
                   ) AS bookmarked
            FROM place p
            WHERE p.id = ?
            """;

        return jdbcTemplate.query(sql, resultSet -> {
            if (!resultSet.next()) {
                return null;
            }
            return new PlaceDetailResponse(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("address"),
                resultSet.getString("phone_number"),
                resultSet.getString("place_type"),
                resultSet.getBoolean("bookmarked")
            );
        }, userId, placeId);
    }

    public List<BookmarkedPlaceResponse> findBookmarkedPlacesByUserId(Long userId) {
        String sql = """
            SELECT p.id, p.name, p.address, p.tel AS phone_number, p.place_type
            FROM place p
            INNER JOIN book_mark b ON b.place_id = p.id
            WHERE b.user_id = ?
            """;

        return jdbcTemplate.query(sql, (resultSet, rowNumber) ->
            new BookmarkedPlaceResponse(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("address"),
                resultSet.getString("phone_number"),
                resultSet.getString("place_type")
            ), userId);
    }

    public void batchUpsert(List<Place> places) {
        if (places.isEmpty()) {
            return;
        }

        String sql = """
            INSERT INTO place (name, place_type, address, tel, active, coordinate)
            VALUES (?, ?, ?, ?, ?, ST_GeomFromText(?, 4326, 'axis-order=long-lat'))
            ON DUPLICATE KEY UPDATE
                place_type = VALUES(place_type),
                address = VALUES(address),
                tel = VALUES(tel),
                active = VALUES(active),
                coordinate = VALUES(coordinate)
            """;

        jdbcTemplate.batchUpdate(sql, places, BATCH_SIZE, (statement, place) -> {
            statement.setString(1, place.getName());
            statement.setString(2, place.getPlaceType().name());
            statement.setString(3, place.getAddress());
            statement.setString(4, place.getPhoneNumber());
            statement.setBoolean(5, place.isActive());
            statement.setString(6, "POINT(%f %f)".formatted(
                place.getCoordinate().getX(),
                place.getCoordinate().getY()
            ));
        });
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
