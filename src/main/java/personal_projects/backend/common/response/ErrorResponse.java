package personal_projects.backend.common.response;

import personal_projects.backend.common.exception.DomainErrorCode;

import java.util.Map;

public record ErrorResponse(String code, String message, Map<String, String> errors) {

    public ErrorResponse {
        errors = Map.copyOf(errors);
    }

    public static ErrorResponse of(DomainErrorCode code, String message) {
        return new ErrorResponse(code.name(), message, Map.of());
    }

    public static ErrorResponse of(DomainErrorCode code, String message, Map<String, String> errors) {
        return new ErrorResponse(code.name(), message, errors);
    }
}
