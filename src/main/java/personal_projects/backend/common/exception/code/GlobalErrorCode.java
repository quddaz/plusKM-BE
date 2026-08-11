package personal_projects.backend.common.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements ErrorCode {

    PLACE_IMPORT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Place import failed."),

    PLACE_IMPORT_FORBIDDEN(HttpStatus.FORBIDDEN, "Place import is not authorized.");
    private final HttpStatus httpStatus;
    private final String message;
}
