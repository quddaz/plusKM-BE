package personal_projects.backend.global.admin.facade;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import personal_projects.backend.global.admin.exception.AdminException;
import personal_projects.backend.global.admin.service.PlaceInitializer;
import personal_projects.backend.global.admin.service.PlaceMongoInitializer;
import personal_projects.backend.global.dto.ResponseTemplate;
import personal_projects.backend.global.exception.errorCode.GlobalErrorCode;

@Component
@Slf4j
@RequiredArgsConstructor
public class CSVManagementFacade {
    private final PlaceInitializer placeInitializer;
    private final PlaceMongoInitializer placeMongoInitializer;

    @Transactional(rollbackOn = Exception.class)
    public ResponseTemplate<?> csvManagement(String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new AdminException(GlobalErrorCode.ADMIN_NOT_AUTHORIZED);
        }

        try {
            placeInitializer.updatePlaceDataFromCsv();
            placeMongoInitializer.updatePlaceDataFromCsv();
        } catch (Exception e) {
            // 로그 기록 및 예외 대응
            log.error("CSV Management Error: ", e);
            throw new AdminException(GlobalErrorCode.FILE_CONVERT_FAIL);
        }

        return ResponseTemplate.EMPTY_RESPONSE;
    }
}
