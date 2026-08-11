package personal_projects.backend.domain.auth.exception.code;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import personal_projects.backend.common.exception.code.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {
    UNSUPPORTED_OAUTH_PROVIDER(HttpStatus.NOT_ACCEPTABLE, "Unsupported OAuth provider."),
    INVALID_REFRESH_TOKEN(HttpStatus.NOT_ACCEPTABLE, "Refresh token is invalid."),
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
