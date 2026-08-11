package personal_projects.backend.global.exception;

import lombok.Getter;
import personal_projects.backend.global.exception.errorCode.ErrorCode;

@Getter
public abstract class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    protected BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
