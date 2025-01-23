package personal_projects.backend.domain.bookmark.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import personal_projects.backend.global.exception.errorCode.ErrorCode;
@Getter
@RequiredArgsConstructor
public class BookMarkNotFoundException extends RuntimeException {
    public final ErrorCode errorCode;
}
