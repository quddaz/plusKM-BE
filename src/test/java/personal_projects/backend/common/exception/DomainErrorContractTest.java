package personal_projects.backend.common.exception;

import org.junit.jupiter.api.Test;
import personal_projects.backend.common.response.ErrorResponse;
import personal_projects.backend.common.exception.code.CommonErrorCode;
import personal_projects.backend.domain.place.exception.code.PlaceErrorCode;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainErrorContractTest {

    @Test
    void domainErrorCode_메시지키와제목키를생성한다() {
        DomainErrorCode code = PlaceErrorCode.PLACE_NOT_FOUND;

        assertEquals("error.place_not_found", code.messageKey());
        assertEquals("error.title.place_not_found", code.titleKey());
    }

    @Test
    void businessException_도메인에러코드만보관한다() {
        BusinessException exception = new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND);

        assertEquals(PlaceErrorCode.PLACE_NOT_FOUND, exception.code());
        assertEquals("PLACE_NOT_FOUND", exception.getMessage());
    }

    @Test
    void 일반에러응답은_빈errors를반환한다() {
        ErrorResponse response = ErrorResponse.of(
            PlaceErrorCode.PLACE_NOT_FOUND,
            "장소를 찾을 수 없습니다."
        );

        assertTrue(response.errors().isEmpty());
    }

    @Test
    void validation에러응답은_필드별errors를반환한다() {
        Map<String, String> errors = Map.of("placeId", "장소 ID는 필수 값입니다.");

        ErrorResponse response = ErrorResponse.of(
            CommonErrorCode.VALIDATION_ERROR,
            "요청값이 올바르지 않습니다.",
            errors
        );

        assertEquals(errors, response.errors());
    }

}
