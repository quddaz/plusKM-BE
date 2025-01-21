package personal_projects.backend.domain.place.exception.errorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import personal_projects.backend.global.exception.errorCode.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum PlaceErrorCode implements ErrorCode {
    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "PLACE not found")
    ;

    private final HttpStatus httpStatus;
    private final String message;
}