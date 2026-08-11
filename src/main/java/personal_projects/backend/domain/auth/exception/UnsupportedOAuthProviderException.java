package personal_projects.backend.domain.auth.exception;


import personal_projects.backend.common.exception.BusinessException;
import personal_projects.backend.common.exception.code.ErrorCode;

public class UnsupportedOAuthProviderException extends BusinessException {

    public UnsupportedOAuthProviderException(ErrorCode errorCode) {
        super(errorCode);
    }
}
