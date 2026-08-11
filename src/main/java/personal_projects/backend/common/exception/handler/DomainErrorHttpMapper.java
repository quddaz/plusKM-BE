package personal_projects.backend.common.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import personal_projects.backend.common.exception.DomainErrorCode;
import personal_projects.backend.common.exception.code.CommonErrorCode;
import personal_projects.backend.domain.auth.exception.code.AuthErrorCode;
import personal_projects.backend.domain.bookmark.exception.code.BookmarkErrorCode;
import personal_projects.backend.domain.medicalrecord.exception.code.MedicalRecordErrorCode;
import personal_projects.backend.domain.place.exception.code.PlaceErrorCode;
import personal_projects.backend.domain.user.exception.code.UserErrorCode;

import java.util.Map;

@Component
public class DomainErrorHttpMapper {

    private static final Map<DomainErrorCode, HttpStatus> STATUSES = Map.ofEntries(
        Map.entry(CommonErrorCode.INVALID_INPUT, HttpStatus.BAD_REQUEST),
        Map.entry(CommonErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST),
        Map.entry(CommonErrorCode.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR),
        Map.entry(AuthErrorCode.AUTHENTICATION_REQUIRED, HttpStatus.UNAUTHORIZED),
        Map.entry(AuthErrorCode.ACCESS_DENIED, HttpStatus.FORBIDDEN),
        Map.entry(AuthErrorCode.UNSUPPORTED_OAUTH_PROVIDER, HttpStatus.NOT_ACCEPTABLE),
        Map.entry(AuthErrorCode.INVALID_REFRESH_TOKEN, HttpStatus.NOT_ACCEPTABLE),
        Map.entry(BookmarkErrorCode.BOOKMARK_NOT_FOUND, HttpStatus.NOT_FOUND),
        Map.entry(MedicalRecordErrorCode.MEDICAL_RECORD_NOT_FOUND, HttpStatus.NOT_FOUND),
        Map.entry(PlaceErrorCode.PLACE_NOT_FOUND, HttpStatus.NOT_FOUND),
        Map.entry(PlaceErrorCode.PLACE_IMPORT_FAILED, HttpStatus.INTERNAL_SERVER_ERROR),
        Map.entry(PlaceErrorCode.PLACE_IMPORT_FORBIDDEN, HttpStatus.FORBIDDEN),
        Map.entry(UserErrorCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND),
        Map.entry(UserErrorCode.USER_BIRTH_NOT_FOUND, HttpStatus.NOT_FOUND)
    );

    public HttpStatus statusOf(DomainErrorCode code) {
        return STATUSES.getOrDefault(code, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
