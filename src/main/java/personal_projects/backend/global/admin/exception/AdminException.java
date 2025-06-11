package personal_projects.backend.global.admin.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import personal_projects.backend.global.exception.errorCode.ErrorCode;

@Getter
@RequiredArgsConstructor
public class AdminException extends RuntimeException{
    private final ErrorCode errorCode;
}
