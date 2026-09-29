package personal_projects.backend.domain.place.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import personal_projects.backend.domain.place.entity.PlaceCategory;

public record NearbyPlaceSearchRequest(
    @DecimalMin("-180.0") @DecimalMax("180.0") double longitude,
    @DecimalMin("-90.0") @DecimalMax("90.0") double latitude,
    @Positive @DecimalMax("100.0") double radiusKilometers,
    @NotNull PlaceCategory category
) {
}
