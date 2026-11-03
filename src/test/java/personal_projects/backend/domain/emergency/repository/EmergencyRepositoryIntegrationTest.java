package personal_projects.backend.domain.emergency.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import personal_projects.backend.domain.emergency.dto.response.NearbyEmergencyResponse;
import personal_projects.backend.domain.emergency.repository.jdbc.EmergencyJdbcRepository;
import personal_projects.backend.support.MySqlIntegrationTest;

@Tag("repository")
class EmergencyRepositoryIntegrationTest extends MySqlIntegrationTest {

    @Autowired EmergencyJdbcRepository repository;

    @Test
    void 정확_반경_조회는_활성_응급실을_거리순으로_정렬하고_좌표를_보존한다() {
        double lon = 127.1048549, lat = 37.4003268;
        var center = emergency("CENTER", true, lon, lat);
        var away = destination(lon, lat, 90, 500);
        var nearby = emergency("NEAR", true, away[0], away[1]);
        emergency("CLOSED", false, lon, lat);
        var corner = destination(lon, lat, 45, 1_200);
        emergency("OUTSIDE", true, corner[0], corner[1]);

        var result = repository.findNearbyEmergenciesOptimized(lon, lat, 1);

        assertThat(result).extracting(NearbyEmergencyResponse::id).containsExactly(center.getId(), nearby.getId());
        assertThat(result.getFirst().longitude()).isCloseTo(lon, within(1e-8));
        assertThat(result.getFirst().latitude()).isCloseTo(lat, within(1e-8));
    }
}
