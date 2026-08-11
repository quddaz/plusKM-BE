package personal_projects.backend.domain.place.importer.exception;

import personal_projects.backend.common.exception.BusinessException;
import personal_projects.backend.common.exception.code.ErrorCode;

public class PlaceImportException extends BusinessException {

    public PlaceImportException(ErrorCode errorCode) {
        super(errorCode);
    }
}
