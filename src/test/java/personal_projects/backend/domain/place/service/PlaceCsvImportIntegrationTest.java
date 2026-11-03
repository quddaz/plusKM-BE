package personal_projects.backend.domain.place.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import personal_projects.backend.domain.favorite.service.FavoriteService;
import personal_projects.backend.domain.place.entity.PlaceCategory;
import personal_projects.backend.domain.user.type.Role;
import personal_projects.backend.support.MySqlIntegrationTest;

@Tag("integration")
class PlaceCsvImportIntegrationTest extends MySqlIntegrationTest {

    @Autowired PlaceCsvImportService service;
    @Autowired FavoriteService favorites;

    @Test
    void 최신_CSV로_값을_갱신하고_누락과_재등장의_ID와_즐겨찾기를_보존한다() {
        var changed = place("갱신 병원", PlaceCategory.HOSPITAL, true, 127.1, 37.4);
        var reappeared = place("재등장 병원", PlaceCategory.HOSPITAL, false, 127.1, 37.4);
        var missing = place("누락 병원", PlaceCategory.HOSPITAL, true, 127.1, 37.4);
        place("갱신 약국", PlaceCategory.PHARMACY, true, 127.1, 37.4);
        var owner = user(Role.USER);
        var favorite = favorites.addPlace(owner.getId(), missing.getId());

        var result = service.importAll();

        assertThat(result.hospitals()).isEqualTo(3);
        assertThat(result.pharmacies()).isEqualTo(1);
        assertThat(result.skipped()).isEqualTo(1);
        assertThat(result.inactivePlaces()).isEqualTo(1);
        var updated = places.findById(changed.getId()).orElseThrow();
        assertThat(updated.getPhoneNumber()).isEqualTo("031-111-2222");
        assertThat(updated.getFacilityType()).isEqualTo("종합병원");
        assertThat(updated.getCoordinate().getX()).isCloseTo(127.105, within(1e-8));
        assertThat(updated.getCoordinate().getY()).isCloseTo(37.401, within(1e-8));
        assertThat(places.findById(reappeared.getId()).orElseThrow().isActive()).isTrue();
        assertThat(places.findById(missing.getId()).orElseThrow().isActive()).isFalse();
        assertThat(favorites.findAll(owner.getId())).singleElement().satisfies(saved -> {
            assertThat(saved.id()).isEqualTo(favorite.id());
            assertThat(saved.targetId()).isEqualTo(missing.getId());
            assertThat(saved.active()).isFalse();
        });
        var idsBefore = jdbc.queryForList("SELECT id FROM place ORDER BY id", Long.class);
        service.importAll();
        assertThat(jdbc.queryForList("SELECT id FROM place ORDER BY id", Long.class)).isEqualTo(idsBefore);
    }

    @Test
    void 활성_행의_절반보다_적은_CSV는_홀수_건수에서도_병합하지_않는다() {
        for (int i = 0; i < 9; i++) place("보존" + i, PlaceCategory.HOSPITAL, true, 127.1, 37.4);

        assertThatThrownBy(service::importAll).isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("스테이징 행 수 검증 실패");

        assertThat(places.count()).isEqualTo(9);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM place WHERE active", Integer.class)).isEqualTo(9);
    }

    @Test
    void 병합_도중_DB_오류가_나면_기존값까지_롤백하고_다음_갱신을_허용한다() {
        var existing = place("갱신 병원", PlaceCategory.HOSPITAL, true, 127.1, 37.4);
        place("갱신 약국", PlaceCategory.PHARMACY, true, 127.1, 37.4);
        jdbc.execute("""
            CREATE TRIGGER reject_place_insert BEFORE INSERT ON place FOR EACH ROW
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '갱신 중 실패 주입'
            """);
        try {
            assertThatThrownBy(service::importAll).isInstanceOf(org.springframework.dao.DataAccessException.class);
            assertThat(places.findById(existing.getId()).orElseThrow().getPhoneNumber()).isEqualTo("031-000-0000");
            assertThat(places.count()).isEqualTo(2);
        } finally {
            jdbc.execute("DROP TRIGGER reject_place_insert");
        }

        service.importAll();
        assertThat(places.count()).isEqualTo(4);
        assertThat(places.findById(existing.getId()).orElseThrow().getPhoneNumber()).isEqualTo("031-111-2222");
    }
}
