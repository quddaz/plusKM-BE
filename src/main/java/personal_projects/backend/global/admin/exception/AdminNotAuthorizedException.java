package personal_projects.backend.global.admin.exception;

import personal_projects.backend.global.exception.BusinessException;
import personal_projects.backend.global.exception.errorCode.ErrorCode;

public class AdminNotAuthorizedException extends BusinessException {

    public AdminNotAuthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }
}
