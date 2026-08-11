package personal_projects.backend.domain.place.exception;

import personal_projects.backend.common.exception.BusinessException;
import personal_projects.backend.common.exception.code.ErrorCode;

public class PlaceNotFoundException extends BusinessException {

    public PlaceNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
