package personal_projects.backend.domain.medicalrecord.exception;

import personal_projects.backend.common.exception.BusinessException;
import personal_projects.backend.common.exception.DomainErrorCode;

public class MedicalRecordNotFoundException extends BusinessException {

    public MedicalRecordNotFoundException(DomainErrorCode code) {
        super(code);
    }
}
