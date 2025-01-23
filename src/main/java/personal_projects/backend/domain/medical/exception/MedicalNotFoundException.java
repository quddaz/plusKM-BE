package personal_projects.backend.domain.medical.exception;

import lombok.RequiredArgsConstructor;
import personal_projects.backend.global.exception.errorCode.ErrorCode;

@RequiredArgsConstructor
public class MedicalNotFoundException extends RuntimeException{
    private final ErrorCode errorCode;
}
