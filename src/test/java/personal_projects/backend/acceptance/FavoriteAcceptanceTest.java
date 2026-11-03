package personal_projects.backend.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PUT;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import personal_projects.backend.domain.place.entity.PlaceCategory;
import personal_projects.backend.domain.user.type.Role;
import personal_projects.backend.support.ApiAcceptanceTest;

class FavoriteAcceptanceTest extends ApiAcceptanceTest {

    @ParameterizedTest
    @ValueSource(strings = {"places", "emergencies"})
    void F01_저장_중복저장_개인목록_해제를_HTTP로_검증한다(String kind) {
        var owner = user(Role.USER);
        var other = user(Role.USER);
        long target = kind.equals("places")
            ? place("병원", PlaceCategory.HOSPITAL, true, 127.1, 37.4).getId()
            : emergency("A0001", true, 127.1, 37.4).getId();
        String path = "/favorites/" + kind + "/" + target;

        var first = call(PUT, path, bearer(owner), null);
        var again = call(PUT, path, bearer(owner), null);
        assertThat(first.getStatusCode().value()).isEqualTo(200);
        assertThat(again.getBody().path("id")).isEqualTo(first.getBody().path("id"));
        assertThat(call(GET, "/favorites", bearer(owner), null).getBody()).hasSize(1);
        assertThat(call(GET, "/favorites", bearer(other), null).getBody()).isEmpty();
        assertThat(call(DELETE, path, bearer(other), null).getStatusCode().value()).isEqualTo(204);
        assertThat(call(GET, "/favorites", bearer(owner), null).getBody()).hasSize(1);
        assertThat(call(DELETE, path, bearer(owner), null).getStatusCode().value()).isEqualTo(204);
        assertThat(call(DELETE, path, bearer(owner), null).getStatusCode().value()).isEqualTo(204);
        assertThat(call(GET, "/favorites", bearer(owner), null).getBody()).isEmpty();
    }

    @Test
    void F02_없는_대상과_비활성_대상을_새로_저장하면_404이다() {
        var owner = user(Role.USER);
        var closed = place("폐업", PlaceCategory.PHARMACY, false, 127.1, 37.4);
        assertThat(call(PUT, "/favorites/places/" + closed.getId(), bearer(owner), null)
            .getStatusCode().value()).isEqualTo(404);
        assertThat(call(PUT, "/favorites/emergencies/99999999", bearer(owner), null)
            .getStatusCode().value()).isEqualTo(404);
    }
}
