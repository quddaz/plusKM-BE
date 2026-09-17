package personal_projects.backend.common.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import personal_projects.backend.domain.emergency.service.EmergencyBedCacheService;

@Component
@RequiredArgsConstructor
public class EmergencyBedSyncScheduler {

    private final EmergencyBedCacheService cacheService;

    @Scheduled(
        fixedDelayString = "${emergency.bed-sync-delay:5m}",
        initialDelayString = "${emergency.bed-sync-initial-delay:0s}"
    )
    public void synchronize() {
        cacheService.refresh();
    }
}
