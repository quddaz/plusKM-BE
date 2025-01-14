package personal_projects.backend.domain.user.exception.errorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import personal_projects.backend.global.exception.errorCode.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "user not found"),
    USER_BIRTH_NOT_FOUND(HttpStatus.NOT_FOUND, "user birth not found");

    private final HttpStatus httpStatus;
    private final String message;
}