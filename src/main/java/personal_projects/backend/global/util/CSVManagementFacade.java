package personal_projects.backend.global.util;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import personal_projects.backend.domain.place.repository.init.PlaceInitializer;
import personal_projects.backend.domain.place.repository.init.PlaceMongoInitializer;
import personal_projects.backend.global.dto.ResponseTemplate;

@Component
@Slf4j
@RequiredArgsConstructor
public class CSVManagementFacade {
    private final PlaceInitializer placeInitializer;
    private final PlaceMongoInitializer placeMongoInitializer;

    @Transactional(rollbackOn = Exception.class)
    public ResponseTemplate<?> csvManagement(String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseTemplate.JSON_ROLE_ERROR;
        }

        try {
            placeInitializer.updatePlaceDataFromCsv();
            placeMongoInitializer.updatePlaceDataFromCsv();
        } catch (Exception e) {
            // 로그 기록 및 예외 대응
            log.error("CSV 데이터 처리 중 예외 발생", e);
            return ResponseTemplate.fail("CSV 데이터 처리 중 오류가 발생했습니다: " + e.getMessage());
        }

        return ResponseTemplate.EMPTY_RESPONSE;
    }
}
