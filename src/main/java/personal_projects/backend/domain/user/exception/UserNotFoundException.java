package personal_projects.backend.domain.user.exception;

import personal_projects.backend.common.exception.BusinessException;
import personal_projects.backend.common.exception.code.ErrorCode;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
