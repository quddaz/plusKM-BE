package personal_projects.backend.domain.medicalrecord.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import personal_projects.backend.common.exception.code.ErrorCode;

@RequiredArgsConstructor
@Getter
public enum MedicalRecordErrorCode implements ErrorCode {
    MEDICAL_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 진료기록을 찾을 수 없습니다."),
    ;
    private final HttpStatus httpStatus;
    private final String message;
}
