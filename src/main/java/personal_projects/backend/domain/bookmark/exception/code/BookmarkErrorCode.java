package personal_projects.backend.domain.bookmark.exception.code;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import personal_projects.backend.common.exception.code.ErrorCode;
@Getter
@RequiredArgsConstructor
public enum BookmarkErrorCode implements ErrorCode {
    BOOKMARK_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOKMARK not found")
    ;
    private final HttpStatus httpStatus;
    private final String message;
}