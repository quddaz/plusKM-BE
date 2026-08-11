package personal_projects.backend.global.admin.exception;

import personal_projects.backend.global.exception.BusinessException;
import personal_projects.backend.global.exception.errorCode.ErrorCode;

public class FileConversionException extends BusinessException {

    public FileConversionException(ErrorCode errorCode) {
        super(errorCode);
    }
}
