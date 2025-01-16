package personal_projects.backend.oauth.exception;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import personal_projects.backend.global.exception.errorCode.ErrorCode;

@Getter
@RequiredArgsConstructor
public class LoginTypeNotSupportException extends RuntimeException {
    private final ErrorCode errorCode;
}