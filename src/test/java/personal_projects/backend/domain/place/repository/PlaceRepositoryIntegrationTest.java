package personal_projects.backend.domain.place.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import personal_projects.backend.domain.place.config.PlaceSearchIndexInitializer;
import personal_projects.backend.domain.place.dto.request.NearbyPlaceSearchRequest;
import personal_projects.backend.domain.place.dto.response.NearbyPlaceResponse;
import personal_projects.backend.domain.place.entity.PlaceCategory;
import personal_projects.backend.support.MySqlIntegrationTest;

@Tag("repository")
class PlaceRepositoryIntegrationTest extends MySqlIntegrationTest {

    private static final double LON = 127.1048549;
    private static final double LAT = 37.4003268;
    @Autowired PlaceJdbcRepository repository;
    @Autowired PlaceSearchIndexInitializer initializer;

    @Test
    void 검색은_활성_장소만_거리와_ID순으로_반환하고_카테고리를_구분한다() {
        var first = place("중심 병원", PlaceCategory.HOSPITAL, true, LON, LAT);
        var second = place("같은 위치 약국", PlaceCategory.PHARMACY, true, LON, LAT);
        var away = destination(LON, LAT, 90, 500);
        var third = place("500m 병원", PlaceCategory.HOSPITAL, true, away[0], away[1]);
        place("비활성", PlaceCategory.HOSPITAL, false, LON, LAT);
        var corner = destination(LON, LAT, 45, 1_200);
        place("사각형 안 반경 밖", PlaceCategory.HOSPITAL, true, corner[0], corner[1]);

        List<NearbyPlaceResponse> result = repository.findNearby(request(1, PlaceCategory.ALL));

        assertThat(result).extracting(NearbyPlaceResponse::id)
            .containsExactly(first.getId(), second.getId(), third.getId());
        assertThat(result.getFirst().longitude()).isCloseTo(LON, within(1e-8));
        assertThat(result.getFirst().latitude()).isCloseTo(LAT, within(1e-8));
        assertThat(result.getLast().distanceMeters()).isBetween(499.99, 500.01);
        assertThat(repository.findNearby(request(1, PlaceCategory.PHARMACY)))
            .extracting(NearbyPlaceResponse::id).containsExactly(second.getId());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.3, 0.5, 1, 3, 5, 10})
    void 각_반경에서_모든_방향의_경계_안쪽은_포함하고_바깥쪽은_제외한다(double radius) {
        List<Long> expected = new ArrayList<>();
        for (int bearing = 0; bearing < 360; bearing += 45) {
            double[] inside = destination(LON, LAT, bearing, radius * 1_000 - 0.2);
            double[] outside = destination(LON, LAT, bearing, radius * 1_000 + 0.2);
            expected.add(place("안쪽" + bearing, PlaceCategory.HOSPITAL, true, inside[0], inside[1]).getId());
            place("바깥쪽" + bearing, PlaceCategory.HOSPITAL, true, outside[0], outside[1]);
        }

        assertThat(repository.findNearby(request(radius, PlaceCategory.ALL)))
            .extracting(NearbyPlaceResponse::id).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void 조회_결과가_200개를_넘어도_잘리지_않는다() {
        List<Object[]> rows = new ArrayList<>();
        for (int i = 0; i < 205; i++) rows.add(new Object[]{"병원" + i, "주소" + i});
        jdbc.batchUpdate("""
            INSERT INTO place (name, address, category, facility_type, active, coordinate)
            VALUES (?, ?, 'HOSPITAL', '의원', true,
                ST_GeomFromText('POINT(127.1048549 37.4003268)', 4326, 'axis-order=long-lat'))
            """, rows);

        assertThat(repository.findNearby(request(1, PlaceCategory.ALL))).hasSize(205);
    }

    @Test
    void 생성_열은_좌표_변경을_따르고_인덱스_초기화는_반복해도_된다() {
        var place = place("좌표 확인", PlaceCategory.HOSPITAL, true, LON, LAT);
        initializer.run(null);
        initializer.run(null);
        jdbc.update("""
            UPDATE place SET coordinate = ST_GeomFromText('POINT(127.11 37.41)', 4326, 'axis-order=long-lat')
            WHERE id = ?
            """, place.getId());

        var coordinates = jdbc.queryForMap("SELECT longitude, latitude FROM place WHERE id = ?", place.getId());
        assertThat(((Number) coordinates.get("longitude")).doubleValue()).isCloseTo(127.11, within(1e-8));
        assertThat(((Number) coordinates.get("latitude")).doubleValue()).isCloseTo(37.41, within(1e-8));
        assertThat(jdbc.queryForObject("""
            SELECT COUNT(DISTINCT index_name) FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'place'
              AND index_name IN ('idx_place_lat_lon', 'idx_place_lon_lat')
            """, Integer.class)).isEqualTo(2);
    }

    private NearbyPlaceSearchRequest request(double radius, PlaceCategory category) {
        return new NearbyPlaceSearchRequest(LON, LAT, radius, category);
    }
}
