package personal_projects.backend.domain.place.exception;

import personal_projects.backend.common.exception.BusinessException;
import personal_projects.backend.common.exception.DomainErrorCode;

public class PlaceNotFoundException extends BusinessException {

    public PlaceNotFoundException(DomainErrorCode code) {
        super(code);
    }
}
