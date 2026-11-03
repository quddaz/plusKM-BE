package personal_projects.backend.domain.place.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import personal_projects.backend.domain.place.dto.PlaceCsvRow;
import personal_projects.backend.domain.place.entity.PlaceCategory;
import personal_projects.backend.support.MySqlIntegrationTest;

@Tag("repository")
class PlaceCsvStageRepositoryIntegrationTest extends MySqlIntegrationTest {

    @Autowired PlaceCsvStageRepository stage;

    @Test
    void 스테이징의_중복은_마지막_값으로_병합하고_누락은_비활성화한다() {
        var existing = place("기존", PlaceCategory.HOSPITAL, true, 127.1, 37.4);
        var missing = place("누락", PlaceCategory.HOSPITAL, true, 127.1, 37.4);
        inTransaction(() -> {
            stage.createStageTable();
            try {
                stage.saveAllToStage(List.of(row("기존", "이전 번호"), row("기존", "새 번호"), row("신규", "신규 번호")));
                assertThat(stage.countStaged()).isEqualTo(2);
                assertThat(stage.updateChangedFromStage()).isEqualTo(1);
                assertThat(stage.insertNewFromStage()).isEqualTo(1);
                assertThat(stage.deactivateMissingFromStage()).isEqualTo(1);
                assertThat(stage.updateChangedFromStage()).isZero();
                assertThat(stage.insertNewFromStage()).isZero();
                assertThat(stage.deactivateMissingFromStage()).isZero();
            } finally {
                stage.dropStageTable();
            }
        });

        assertThat(places.findById(existing.getId()).orElseThrow().getPhoneNumber()).isEqualTo("새 번호");
        assertThat(places.findById(missing.getId()).orElseThrow().isActive()).isFalse();
        assertThat(places.count()).isEqualTo(3);
    }

    @Test
    void 같은_이름과_주소의_비활성_장소는_ID를_유지하며_재활성화한다() {
        var existing = place("재등장", PlaceCategory.HOSPITAL, false, 127.1, 37.4);
        inTransaction(() -> {
            stage.createStageTable();
            try {
                stage.saveAllToStage(List.of(row("재등장", "031-123-4567")));
                assertThat(stage.updateChangedFromStage()).isEqualTo(1);
                assertThat(stage.insertNewFromStage()).isZero();
            } finally {
                stage.dropStageTable();
            }
        });

        var reactivated = places.findById(existing.getId()).orElseThrow();
        assertThat(reactivated.isActive()).isTrue();
        assertThat(reactivated.getCoordinate().getX()).isEqualTo(127.2);
        assertThat(reactivated.getCoordinate().getY()).isEqualTo(37.5);
    }

    private PlaceCsvRow row(String name, String phone) {
        return new PlaceCsvRow(name, PlaceCategory.HOSPITAL, "의원", name + " 주소", phone, 127.2, 37.5);
    }
}
