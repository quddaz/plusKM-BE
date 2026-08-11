package personal_projects.backend.domain.bookmark.exception;

import personal_projects.backend.common.exception.BusinessException;
import personal_projects.backend.common.exception.code.ErrorCode;

public class BookmarkNotFoundException extends BusinessException {

    public BookmarkNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
