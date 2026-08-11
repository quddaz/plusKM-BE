package personal_projects.backend.domain.bookmark.exception;

import personal_projects.backend.common.exception.BusinessException;
import personal_projects.backend.common.exception.DomainErrorCode;

public class BookmarkNotFoundException extends BusinessException {

    public BookmarkNotFoundException(DomainErrorCode code) {
        super(code);
    }
}
