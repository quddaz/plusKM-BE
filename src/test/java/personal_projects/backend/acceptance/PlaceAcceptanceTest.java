package personal_projects.backend.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import personal_projects.backend.domain.place.entity.PlaceCategory;
import personal_projects.backend.support.ApiAcceptanceTest;

class PlaceAcceptanceTest extends ApiAcceptanceTest {

    @Test
    void P01_로그인_없이_주변_활성_병원과_약국을_조회하고_카테고리로_좁힌다() {
        var hospital = place("병원", PlaceCategory.HOSPITAL, true, 127.1, 37.4);
        place("약국", PlaceCategory.PHARMACY, true, 127.1, 37.4);
        place("비활성 병원", PlaceCategory.HOSPITAL, false, 127.1, 37.4);

        var all = call(POST, "/places/search", null, request(3, "ALL"));
        var hospitals = call(POST, "/places/search", null, request(3, "HOSPITAL"));

        assertThat(all.getStatusCode().value()).isEqualTo(200);
        assertThat(all.getBody().path("places")).hasSize(2);
        assertThat(hospitals.getBody().path("places")).hasSize(1);
        assertThat(hospitals.getBody().at("/places/0/id").asLong()).isEqualTo(hospital.getId());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.3, 0.5, 1, 3, 5, 10})
    void P02_지원하는_검색_반경은_빈_결과도_200으로_반환한다(double radius) {
        var response = call(POST, "/places/search", null, request(radius, "ALL"));
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().path("places")).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1, 0, 0.2, 2, 10.001, 30})
    void P03_지원하지_않는_반경은_400이다(double radius) {
        assertThat(call(POST, "/places/search", null, request(radius, "ALL")).getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void P04_잘못된_좌표와_카테고리와_필수값_누락은_400이다() {
        assertThat(call(POST, "/places/search", null,
            Map.of("longitude", 181, "latitude", 37.4, "radiusKilometers", 3, "category", "ALL"))
            .getStatusCode().value()).isEqualTo(400);
        assertThat(call(POST, "/places/search", null, request(3, "INVALID")).getStatusCode().value()).isEqualTo(400);
        assertThat(call(POST, "/places/search", null, Map.of()).getStatusCode().value()).isEqualTo(400);
    }

    private Map<String, Object> request(double radius, String category) {
        return Map.of("longitude", 127.1, "latitude", 37.4, "radiusKilometers", radius, "category", category);
    }
}
