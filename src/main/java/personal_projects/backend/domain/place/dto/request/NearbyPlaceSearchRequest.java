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
    @Positive @DecimalMax("3.0") double radiusKilometers,
    @NotNull PlaceCategory category
) {
    @AssertTrue(message = "병원·약국 검색 반경은 1km 또는 3km만 가능합니다.")
    public boolean isSupportedRadius() {
        return radiusKilometers == 1.0 || radiusKilometers == 3.0;
    }
}
