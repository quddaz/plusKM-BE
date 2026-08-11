package personal_projects.backend.domain.place.exception.code;

import personal_projects.backend.common.exception.DomainErrorCode;

public enum PlaceErrorCode implements DomainErrorCode {
    PLACE_NOT_FOUND,
    PLACE_IMPORT_FAILED,
    PLACE_IMPORT_FORBIDDEN
}
