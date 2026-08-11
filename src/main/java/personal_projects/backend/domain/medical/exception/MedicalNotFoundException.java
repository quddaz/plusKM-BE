package personal_projects.backend.domain.medical.exception;

import personal_projects.backend.global.exception.BusinessException;
import personal_projects.backend.global.exception.errorCode.ErrorCode;

public class MedicalNotFoundException extends BusinessException {

    public MedicalNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
