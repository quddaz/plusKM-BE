package personal_projects.backend.domain.emergency.cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import personal_projects.backend.domain.emergency.client.EmergencyBedData;

class EmergencyBedCacheTest {

    private EmergencyBedCache cache;

    @BeforeEach
    void setUp() {
        cache = new EmergencyBedCache(new ConcurrentMapCacheManager("emergencyBeds"));
    }

    @Test
    void 요청한_병원의_병상정보만_조회한다() {
        cache.replaceAll(List.of(
            bed("A1", 3),
            bed("A2", 7)
        ));

        Map<String, EmergencyBedData> result = cache.findAll(List.of("A2", "A3"));

        assertThat(result).containsOnlyKeys("A2");
        assertThat(result.get("A2").emergencyRoom()).isEqualTo(7);
    }

    @Test
    void 새_목록으로_기존_캐시를_전체_교체한다() {
        cache.replaceAll(List.of(bed("A1", 3)));
        cache.replaceAll(List.of(bed("A2", 7)));

        assertThat(cache.findAll(List.of("A1", "A2"))).containsOnlyKeys("A2");
    }

    private EmergencyBedData bed(String hpid, int emergencyRoom) {
        return new EmergencyBedData(hpid, emergencyRoom, null, null, null, "20260917123000");
    }
}
