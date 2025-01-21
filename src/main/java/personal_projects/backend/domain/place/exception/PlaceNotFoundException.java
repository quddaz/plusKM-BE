package personal_projects.backend.domain.place.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import personal_projects.backend.global.exception.errorCode.ErrorCode;

@Getter
@RequiredArgsConstructor
public class PlaceNotFoundException extends RuntimeException{
    private final ErrorCode errorCode;
}
