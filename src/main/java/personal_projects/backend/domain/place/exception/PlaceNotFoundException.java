package personal_projects.backend.domain.place.exception;

import personal_projects.backend.global.exception.BusinessException;
import personal_projects.backend.global.exception.errorCode.ErrorCode;

public class PlaceNotFoundException extends BusinessException {

    public PlaceNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
