package personal_projects.backend.domain.medicalrecord.exception;

import personal_projects.backend.common.exception.BusinessException;
import personal_projects.backend.common.exception.code.ErrorCode;

public class MedicalRecordNotFoundException extends BusinessException {

    public MedicalRecordNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
