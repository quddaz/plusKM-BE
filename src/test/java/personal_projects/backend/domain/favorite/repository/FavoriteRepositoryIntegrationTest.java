package personal_projects.backend.domain.favorite.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import personal_projects.backend.domain.favorite.entity.Favorite;
import personal_projects.backend.domain.place.entity.PlaceCategory;
import personal_projects.backend.domain.user.type.Role;
import personal_projects.backend.support.MySqlIntegrationTest;

@Tag("repository")
class FavoriteRepositoryIntegrationTest extends MySqlIntegrationTest {

    @Autowired FavoriteRepository repository;

    @Test
    void 사용자별_대상_중복은_DB에서도_막고_다른_사용자의_저장은_허용한다() {
        var owner = user(Role.USER);
        var other = user(Role.USER);
        var place = place("병원", PlaceCategory.HOSPITAL, true, 127.1, 37.4);
        repository.saveAndFlush(Favorite.place(owner, place));

        assertThatThrownBy(() -> repository.saveAndFlush(Favorite.place(owner, place)))
            .isInstanceOf(DataIntegrityViolationException.class);
        repository.saveAndFlush(Favorite.place(other, place));
        assertThat(repository.findAllByUser_IdOrderByIdDesc(owner.getId())).hasSize(1);
        assertThat(repository.findAllByUser_IdOrderByIdDesc(other.getId())).hasSize(1);
    }

    @Test
    void 장소와_응급실을_함께_조회하고_다른_사용자의_목록은_섞이지_않는다() {
        var owner = user(Role.USER);
        var other = user(Role.USER);
        var place = place("약국", PlaceCategory.PHARMACY, true, 127.1, 37.4);
        var emergency = emergency("A0001", true, 127.1, 37.4);
        var first = repository.saveAndFlush(Favorite.place(owner, place));
        var second = repository.saveAndFlush(Favorite.emergency(owner, emergency));
        repository.saveAndFlush(Favorite.emergency(other, emergency));

        assertThat(repository.findAllByUser_IdOrderByIdDesc(owner.getId()))
            .extracting(Favorite::getId).containsExactly(second.getId(), first.getId());
        assertThat(repository.findByUser_IdAndPlace_Id(other.getId(), place.getId())).isEmpty();
    }
}
