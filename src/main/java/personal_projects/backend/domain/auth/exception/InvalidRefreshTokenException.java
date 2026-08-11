package personal_projects.backend.domain.auth.exception;

import personal_projects.backend.common.exception.BusinessException;
import personal_projects.backend.common.exception.code.ErrorCode;

public class InvalidRefreshTokenException extends BusinessException {

    public InvalidRefreshTokenException(ErrorCode errorCode) {
        super(errorCode);
    }
}
