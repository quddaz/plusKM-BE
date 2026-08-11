package personal_projects.backend.domain.oauth.exception;


import personal_projects.backend.global.exception.BusinessException;
import personal_projects.backend.global.exception.errorCode.ErrorCode;

public class LoginTypeNotSupportException extends BusinessException {

    public LoginTypeNotSupportException(ErrorCode errorCode) {
        super(errorCode);
    }
}
