package personal_projects.backend.common.scheduler;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import personal_projects.backend.domain.emergency.service.EmergencySyncService;

@ExtendWith(MockitoExtension.class)
class EmergencySyncSchedulerTest {
    @Mock
    private EmergencySyncService syncService;

    @Test
    void 스케줄실행을_동기화서비스에_위임한다() {
        EmergencySyncScheduler scheduler = new EmergencySyncScheduler(syncService);

        scheduler.synchronize();

        verify(syncService).synchronize();
    }
}
