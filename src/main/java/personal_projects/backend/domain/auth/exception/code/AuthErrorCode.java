package personal_projects.backend.domain.auth.exception.code;

import personal_projects.backend.common.exception.DomainErrorCode;

public enum AuthErrorCode implements DomainErrorCode {
    AUTHENTICATION_REQUIRED,
    ACCESS_DENIED,
    UNSUPPORTED_OAUTH_PROVIDER,
    INVALID_REFRESH_TOKEN
}
