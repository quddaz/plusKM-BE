package personal_projects.backend.domain.place.importer.exception;

import personal_projects.backend.common.exception.BusinessException;
import personal_projects.backend.common.exception.DomainErrorCode;

public class PlaceImportForbiddenException extends BusinessException {

    public PlaceImportForbiddenException(DomainErrorCode code) {
        super(code);
    }
}
