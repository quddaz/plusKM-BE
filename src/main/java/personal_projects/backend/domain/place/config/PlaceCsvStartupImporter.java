package personal_projects.backend.domain.place.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import personal_projects.backend.domain.place.dto.response.PlaceCsvImportResponse;
import personal_projects.backend.domain.place.service.PlaceCsvImportService;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "csv", name = "import-on-startup", havingValue = "true")
public class PlaceCsvStartupImporter implements ApplicationRunner {

    private final PlaceCsvImportService importService;

    @Override
    public void run(ApplicationArguments arguments) {
        log.info("병원·약국 CSV 시작 적재를 실행합니다.");
        PlaceCsvImportResponse result = importService.importAll();
        log.info("병원·약국 CSV 시작 적재가 완료되었습니다: {}", result);
    }
}
