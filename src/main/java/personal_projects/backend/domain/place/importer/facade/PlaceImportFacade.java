package personal_projects.backend.domain.place.importer.facade;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import personal_projects.backend.domain.place.importer.exception.PlaceImportForbiddenException;
import personal_projects.backend.domain.place.importer.exception.PlaceImportException;
import personal_projects.backend.domain.place.importer.service.PlaceCsvImporter;
import personal_projects.backend.common.exception.DomainErrorCode;
import personal_projects.backend.domain.user.type.Role;

@Component
@Slf4j
@RequiredArgsConstructor
public class PlaceImportFacade {
    private final PlaceCsvImporter placeCsvImporter;

    @Transactional(rollbackOn = Exception.class)
    public void importPlaces(String role) {
        if (!Role.ADMIN.authority().equals(role)) {
            throw new PlaceImportForbiddenException(DomainErrorCode.PLACE_IMPORT_FORBIDDEN);
        }

        try {
            placeCsvImporter.importPlaces();
        } catch (Exception e) {
            log.error("Failed to import place CSV data", e);
            throw new PlaceImportException(DomainErrorCode.PLACE_IMPORT_FAILED);
        }
    }
}
