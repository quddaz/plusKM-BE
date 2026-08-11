package personal_projects.backend.common.exception;

import java.util.Locale;

public enum DomainErrorCode {
    INVALID_INPUT,
    VALIDATION_ERROR,
    INTERNAL_SERVER_ERROR,
    AUTHENTICATION_REQUIRED,
    ACCESS_DENIED,
    USER_NOT_FOUND,
    USER_BIRTH_NOT_FOUND,
    PLACE_NOT_FOUND,
    PLACE_IMPORT_FAILED,
    PLACE_IMPORT_FORBIDDEN,
    BOOKMARK_NOT_FOUND,
    MEDICAL_RECORD_NOT_FOUND,
    UNSUPPORTED_OAUTH_PROVIDER,
    INVALID_REFRESH_TOKEN;

    public String messageKey() {
        return "error." + name().toLowerCase(Locale.ROOT);
    }

    public String titleKey() {
        return "error.title." + name().toLowerCase(Locale.ROOT);
    }
}
