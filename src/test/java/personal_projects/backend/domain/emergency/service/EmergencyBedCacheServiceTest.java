package personal_projects.backend.domain.emergency.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import personal_projects.backend.domain.emergency.cache.EmergencyBedCache;
import personal_projects.backend.domain.emergency.client.EmergencyBedApiClient;
import personal_projects.backend.domain.emergency.client.EmergencyBedData;

@ExtendWith(MockitoExtension.class)
class EmergencyBedCacheServiceTest {

    @InjectMocks
    private EmergencyBedCacheService service;

    @Mock
    private EmergencyBedApiClient apiClient;

    @Mock
    private EmergencyBedCache cache;

    @Test
    void 전체_병상정보를_받은_뒤_캐시를_교체한다() {
        List<EmergencyBedData> beds = List.of(bed("A1"));
        when(apiClient.fetchBeds()).thenReturn(beds);

        service.refresh();

        verify(cache).replaceAll(beds);
    }

    @Test
    void 빈_응답이면_기존_캐시를_교체하지_않는다() {
        when(apiClient.fetchBeds()).thenReturn(List.of());

        assertThatThrownBy(service::refresh)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("빈 목록");
        verify(cache, never()).replaceAll(List.of());
    }

    private EmergencyBedData bed(String hpid) {
        return new EmergencyBedData(hpid, 3, 2, 1, 5, "20260917123000");
    }
}
