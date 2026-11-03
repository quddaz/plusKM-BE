package personal_projects.backend.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import java.util.Map;
import org.junit.jupiter.api.Test;
import personal_projects.backend.domain.place.entity.PlaceCategory;
import personal_projects.backend.domain.user.type.Role;
import personal_projects.backend.support.ApiAcceptanceTest;

class CsvImportAcceptanceTest extends ApiAcceptanceTest {

    @Test
    void C01_CSV_최신화는_관리자만_실행한다() {
        assertThat(call(POST, "/admin/places/import-csv", null, null).getStatusCode().value()).isEqualTo(401);
        assertThat(call(POST, "/admin/places/import-csv", bearer(user(Role.USER)), null).getStatusCode().value()).isEqualTo(403);
        assertThat(places.count()).isZero();
    }

    @Test
    void C02_관리자_최신화_후_누락_장소는_검색에서_빠지고_즐겨찾기에는_비활성으로_남는다() {
        var owner = user(Role.USER);
        var admin = user(Role.ADMIN);
        var missing = place("누락 병원", PlaceCategory.HOSPITAL, true, 127.105, 37.401);
        var changed = place("갱신 병원", PlaceCategory.HOSPITAL, true, 127.105, 37.401);
        call(PUT, "/favorites/places/" + missing.getId(), bearer(owner), null);

        var imported = call(POST, "/admin/places/import-csv", bearer(admin), null);
        assertThat(imported.getStatusCode().value()).isEqualTo(200);
        assertThat(imported.getBody().path("hospitals").asInt()).isEqualTo(3);
        assertThat(imported.getBody().path("inactivePlaces").asInt()).isEqualTo(1);
        var saved = call(GET, "/favorites", bearer(owner), null).getBody();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).path("targetId").asLong()).isEqualTo(missing.getId());
        assertThat(saved.get(0).path("active").asBoolean()).isFalse();
        var found = call(POST, "/places/search", null, Map.of(
            "longitude", 127.105, "latitude", 37.401, "radiusKilometers", 1, "category", "ALL")).getBody().path("places");
        assertThat(found).hasSize(4);
        assertThat(found.findValuesAsText("id")).contains(changed.getId().toString()).doesNotContain(missing.getId().toString());
    }
}
