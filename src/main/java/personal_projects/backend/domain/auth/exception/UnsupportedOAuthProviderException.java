package personal_projects.backend.domain.auth.exception;


import personal_projects.backend.common.exception.BusinessException;
import personal_projects.backend.common.exception.DomainErrorCode;

public class UnsupportedOAuthProviderException extends BusinessException {

    public UnsupportedOAuthProviderException(DomainErrorCode code) {
        super(code);
    }
}
