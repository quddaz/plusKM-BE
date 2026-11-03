package personal_projects.backend.domain.favorite.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import personal_projects.backend.domain.place.entity.PlaceCategory;
import personal_projects.backend.domain.user.type.Role;
import personal_projects.backend.support.MySqlIntegrationTest;

@Tag("integration")
class FavoriteIntegrationTest extends MySqlIntegrationTest {

    @Autowired FavoriteService service;

    @Test
    void 중복_저장은_같은_즐겨찾기를_반환하고_다른_사용자의_삭제는_영향을_주지_않는다() {
        var owner = user(Role.USER);
        var other = user(Role.USER);
        var place = place("병원", PlaceCategory.HOSPITAL, true, 127.1, 37.4);
        var first = service.addPlace(owner.getId(), place.getId());

        assertThat(service.addPlace(owner.getId(), place.getId()).id()).isEqualTo(first.id());
        service.removePlace(other.getId(), place.getId());
        assertThat(service.findAll(owner.getId())).hasSize(1);
        assertThat(service.findAll(other.getId())).isEmpty();
        service.removePlace(owner.getId(), place.getId());
        service.removePlace(owner.getId(), place.getId());
        assertThat(service.findAll(owner.getId())).isEmpty();
    }

    @Test
    void 비활성_기관의_신규_즐겨찾기는_허용하지_않는다() {
        var owner = user(Role.USER);
        var place = place("휴업 병원", PlaceCategory.HOSPITAL, false, 127.1, 37.4);
        var emergency = emergency("CLOSED", false, 127.1, 37.4);

        assertThatThrownBy(() -> service.addPlace(owner.getId(), place.getId()))
            .isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(() -> service.addEmergency(owner.getId(), emergency.getId()))
            .isInstanceOf(NoSuchElementException.class);
        assertThat(service.findAll(owner.getId())).isEmpty();
    }
}
