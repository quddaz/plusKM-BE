package personal_projects.backend.domain.user.exception;

import personal_projects.backend.common.exception.BusinessException;
import personal_projects.backend.common.exception.DomainErrorCode;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException(DomainErrorCode code) {
        super(code);
    }
}
