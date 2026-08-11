package personal_projects.backend.domain.user.exception;

import personal_projects.backend.global.exception.BusinessException;
import personal_projects.backend.global.exception.errorCode.ErrorCode;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
