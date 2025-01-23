package personal_projects.backend.domain.medical.exception.errorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import personal_projects.backend.global.exception.errorCode.ErrorCode;

@RequiredArgsConstructor
@Getter
public enum MedicalErrorCode implements ErrorCode {
    MEDICAL_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 진료기록을 찾을 수 없습니다."),
    ;
    private final HttpStatus httpStatus;
    private final String message;
}
