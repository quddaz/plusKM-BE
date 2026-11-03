package personal_projects.backend.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import personal_projects.backend.domain.emergency.cache.EmergencyBedCache;
import personal_projects.backend.domain.emergency.client.EmergencyBedData;
import personal_projects.backend.support.ApiAcceptanceTest;

class EmergencyAcceptanceTest extends ApiAcceptanceTest {

    @Autowired EmergencyBedCache cache;

    @ParameterizedTest
    @ValueSource(doubles = {1, 5, 10, 20, 30})
    void E01_주변_응급실은_로그인_없이_조회한다(double radius) {
        var emergency = emergency("A0001", true, 127.1, 37.4);
        var response = call(POST, "/emergencies/search/optimized", null, Map.of(
            "longitude", 127.1, "latitude", 37.4, "radiusKilometers", radius));
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().at("/emergencies/0/id").asLong()).isEqualTo(emergency.getId());
    }

    @Test
    void E02_없는_응급실_상세는_404이고_잘못된_검색_입력은_400이다() {
        assertThat(call(GET, "/emergencies/99999999", null, null).getStatusCode().value()).isEqualTo(404);
        assertThat(call(POST, "/emergencies/search/optimized", null, Map.of(
            "longitude", 127.1, "latitude", 37.4, "radiusKilometers", 0)).getStatusCode().value()).isEqualTo(400);
        assertThat(call(POST, "/emergencies/search/optimized", null, Map.of(
            "longitude", 181, "latitude", 37.4, "radiusKilometers", 5)).getStatusCode().value()).isEqualTo(400);
        assertThat(call(POST, "/emergencies/search/optimized", null, Map.of(
            "longitude", 127.1, "latitude", 37.4, "radiusKilometers", 30.001)).getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void E03_병상_조회는_요청_HPID의_캐시만_읽고_외부_API를_호출하지_않는다() {
        cache.replaceAll(List.of(new EmergencyBedData("A0001", 2, 1, 3, 4, "202611031000"),
            new EmergencyBedData("A0002", 9, 1, 3, 4, "202611031000")));
        var result = call(POST, "/emergencies/beds", null, Map.of("hpids", List.of("A0001", "A0001", "UNKNOWN")));
        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody().path("availabilities")).hasSize(1);
        assertThat(result.getBody().at("/availabilities/0/hpid").asText()).isEqualTo("A0001");
        verifyNoInteractions(bedApi);
        assertThat(call(POST, "/emergencies/beds", null, Map.of("hpids", List.of())).getStatusCode().value()).isEqualTo(400);
        assertThat(call(POST, "/emergencies/beds", null, Map.of("hpids", java.util.Collections.nCopies(201, "A0001")))
            .getStatusCode().value()).isEqualTo(400);
    }
}
