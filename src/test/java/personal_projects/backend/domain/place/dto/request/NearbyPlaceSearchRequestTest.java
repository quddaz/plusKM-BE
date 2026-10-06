package personal_projects.backend.domain.place.dto.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;
import personal_projects.backend.domain.place.entity.PlaceCategory;

class NearbyPlaceSearchRequestTest {

    @Test
    void 병원과_약국의_검색_반경은_최대_10km이다() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();

            assertTrue(validator.validate(request(10.0)).isEmpty());
            assertFalse(validator.validate(request(10.001)).isEmpty());
            assertFalse(validator.validate(request(30.0)).isEmpty());
            assertEquals(1, validator.validate(request(30.0)).size());
        }
    }

    private NearbyPlaceSearchRequest request(double radiusKilometers) {
        return new NearbyPlaceSearchRequest(127.1112, 37.3948, radiusKilometers,
            PlaceCategory.ALL);
    }
}
