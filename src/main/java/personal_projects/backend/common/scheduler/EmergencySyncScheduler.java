package personal_projects.backend.common.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import personal_projects.backend.domain.emergency.importer.EmergencyImporter;

@Component
@RequiredArgsConstructor
public class EmergencySyncScheduler {
    private final EmergencyImporter emergencyImporter;

    @Scheduled(fixedDelayString = "${emergency.sync-delay:24h}")
    public void synchronize() {
        emergencyImporter.importAll();
    }
}
