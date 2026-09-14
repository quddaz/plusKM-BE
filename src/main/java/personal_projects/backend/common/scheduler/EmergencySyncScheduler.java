package personal_projects.backend.common.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import personal_projects.backend.domain.emergency.service.EmergencySyncService;

@Component
@RequiredArgsConstructor
public class EmergencySyncScheduler {
    private final EmergencySyncService syncService;

    @Scheduled(fixedDelayString = "${emergency.sync-delay:24h}")
    public void synchronize() {
        syncService.synchronize();
    }
}
