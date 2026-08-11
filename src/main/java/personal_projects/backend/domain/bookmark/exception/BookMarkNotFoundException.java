package personal_projects.backend.domain.bookmark.exception;

import personal_projects.backend.global.exception.BusinessException;
import personal_projects.backend.global.exception.errorCode.ErrorCode;

public class BookMarkNotFoundException extends BusinessException {

    public BookMarkNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
