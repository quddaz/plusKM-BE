package personal_projects.backend.domain.place.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import personal_projects.backend.domain.place.entity.PlaceCategory;

public record NearbyPlaceSearchRequest(
    @DecimalMin("-180.0") @DecimalMax("180.0") double longitude,
    @DecimalMin("-90.0") @DecimalMax("90.0") double latitude,
    @Positive @DecimalMax("10.0") double radiusKilometers,
    @NotNull PlaceCategory category
) {
    @AssertTrue(message = "병원·약국 검색 반경은 300m, 500m, 1km, 3km, 5km, 10km만 가능합니다.")
    public boolean isSupportedRadius() {
        return radiusKilometers == 0.3 || radiusKilometers == 0.5
            || radiusKilometers == 1.0 || radiusKilometers == 3.0
            || radiusKilometers == 5.0 || radiusKilometers == 10.0;
    }
}
