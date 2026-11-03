package personal_projects.backend.domain.emergency.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import personal_projects.backend.domain.emergency.client.EmergencyBedData;
import personal_projects.backend.domain.emergency.client.EmergencyFacilityData;
import personal_projects.backend.domain.emergency.dto.response.EmergencyBedResponse;
import personal_projects.backend.domain.emergency.importer.EmergencyImporter;
import personal_projects.backend.domain.favorite.service.FavoriteService;
import personal_projects.backend.domain.user.type.Role;
import personal_projects.backend.support.MySqlIntegrationTest;

@Tag("integration")
class EmergencyIntegrationTest extends MySqlIntegrationTest {

    @Autowired EmergencyImporter importer;
    @Autowired EmergencyBedCacheService beds;
    @Autowired FavoriteService favorites;

    @Test
    void HPID로_응급실을_갱신하고_누락과_재등장_시_ID와_즐겨찾기를_보존한다() {
        var original = emergency("A0001", true, 127.1, 37.4);
        var missing = emergency("MISSING", true, 127.1, 37.4);
        var owner = user(Role.USER);
        favorites.addEmergency(owner.getId(), original.getId());
        when(emergencyApi.fetchFacilities()).thenReturn(List.of(new EmergencyFacilityData(
            "A0001", "이름 변경 응급실", "변경 주소", "031-111-2222", 127.11, 37.41)));

        importer.importAll();

        assertThat(emergencies.findById(original.getId()).orElseThrow().getName()).isEqualTo("이름 변경 응급실");
        assertThat(emergencies.findById(missing.getId()).orElseThrow().isActive()).isFalse();
        when(emergencyApi.fetchFacilities()).thenReturn(List.of(
            new EmergencyFacilityData("A0001", "이름 변경 응급실", "변경 주소", "031", 127.11, 37.41),
            new EmergencyFacilityData("MISSING", "재등장", "새 주소", "031", 127.1, 37.4)));
        importer.importAll();
        assertThat(emergencies.findById(missing.getId()).orElseThrow().isActive()).isTrue();
        assertThat(emergencies.count()).isEqualTo(2);
        assertThat(favorites.findAll(owner.getId()).getFirst().name()).isEqualTo("이름 변경 응급실");
    }

    @Test
    void 비어_있거나_실패한_기본정보_갱신은_기존_응급실을_비활성화하지_않는다() {
        var existing = emergency("A0001", true, 127.1, 37.4);
        when(emergencyApi.fetchFacilities()).thenReturn(List.of());
        assertThatThrownBy(importer::importAll).isInstanceOf(IllegalStateException.class);
        assertThat(emergencies.findById(existing.getId()).orElseThrow().isActive()).isTrue();
        when(emergencyApi.fetchFacilities()).thenThrow(new IllegalStateException("API 실패"));
        assertThatThrownBy(importer::importAll).isInstanceOf(IllegalStateException.class);
        assertThat(emergencies.findById(existing.getId()).orElseThrow().isActive()).isTrue();
    }

    @Test
    void 병상은_요청한_HPID만_캐시에서_조회하며_실패한_갱신은_기존값을_보존한다() {
        assertThat(beds.findBeds(List.of("A0001"))).isEmpty();
        verifyNoInteractions(bedApi);
        when(bedApi.fetchBeds()).thenReturn(List.of(
            new EmergencyBedData("A0001", 3, 1, 2, 4, "202611031000"),
            new EmergencyBedData("A0002", 7, 1, 2, 4, "202611031000")));
        beds.refresh();
        assertThat(beds.findBeds(List.of("A0001", "A0001", "UNKNOWN")))
            .extracting(EmergencyBedResponse::hpid).containsExactly("A0001");
        when(bedApi.fetchBeds()).thenReturn(List.of());
        assertThatThrownBy(beds::refresh).isInstanceOf(IllegalStateException.class);
        assertThat(beds.findBeds(List.of("A0001")).getFirst().emergencyRoom()).isEqualTo(3);
        when(bedApi.fetchBeds()).thenThrow(new IllegalStateException("공공 API 실패"));
        assertThatThrownBy(beds::refresh).isInstanceOf(IllegalStateException.class);
        assertThat(beds.findBeds(List.of("A0001")).getFirst().emergencyRoom()).isEqualTo(3);
    }
}
