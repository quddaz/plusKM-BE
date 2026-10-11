package personal_projects.backend.common.scheduler;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import personal_projects.backend.domain.emergency.service.EmergencyBedCacheService;

@ExtendWith(MockitoExtension.class)
class EmergencyBedSyncSchedulerTest {

    @Mock
    private EmergencyBedCacheService cacheService;

    @Test
    void 스케줄실행을_병상캐시서비스에_위임한다() {
        EmergencyBedSyncScheduler scheduler = new EmergencyBedSyncScheduler(cacheService);

        scheduler.synchronize();

        verify(cacheService).refresh();
    }
}
