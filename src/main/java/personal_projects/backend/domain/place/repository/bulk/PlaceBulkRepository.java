package personal_projects.backend.domain.place.repository.bulk;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import personal_projects.backend.domain.place.domain.Place;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class PlaceBulkRepository {
    private final JdbcTemplate jdbcTemplate;
    private static final int BULK_COUNT = 1000;
    // 벌크 INSERT
    public void batchInsertPlaces(List<Place> places) {
        if (places.isEmpty()) return;
        String sql = "INSERT INTO place (name, place_type, address, tel, active, coordinate) " +
            "VALUES (?, ?, ?, ?, ?, ST_GeomFromText(?, 4326)) " +
            "ON DUPLICATE KEY UPDATE " +
            "place_type = VALUES(place_type), " +
            "address = VALUES(address), " +
            "tel = VALUES(tel), " +
            "active = VALUES(active), " +
            "coordinate = VALUES(coordinate)";

        jdbcTemplate.batchUpdate(sql, places, BULK_COUNT, (ps, place) -> {
            ps.setString(1, place.getName());
            ps.setString(2, place.getPlace_type().name());
            ps.setString(3, place.getAddress());
            ps.setString(4, place.getTel());
            ps.setBoolean(5, place.isActive());
            ps.setString(6, String.format("POINT(%f %f)", place.getCoordinate().getX(), place.getCoordinate().getY()));
        });
    }
/*
    // 벌크 DELETE
    public void batchDeletePlaces(List<Long> placeIds) {
        if (placeIds.isEmpty()) return;

        String sql = "DELETE FROM place WHERE id = ?";

        jdbcTemplate.batchUpdate(sql, placeIds, BULK_COUNT, (ps, id) -> ps.setLong(1, id));

        log.info("[PlaceBulkRepository] {} 개의 Place 데이터 벌크 삭제 완료", placeIds.size());
    }
 */
}
