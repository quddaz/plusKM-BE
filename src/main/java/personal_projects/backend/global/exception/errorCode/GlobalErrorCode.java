package personal_projects.backend.global.exception.errorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements ErrorCode {

    FILE_CONVERT_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "file convert failed"),

    ADMIN_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "admin not authorized");
    private final HttpStatus httpStatus;
    private final String message;
}