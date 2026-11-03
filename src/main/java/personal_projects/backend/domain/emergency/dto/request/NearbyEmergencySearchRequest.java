package personal_projects.backend.domain.emergency.dto.request;

import lombok.Builder;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
@Builder
public record NearbyEmergencySearchRequest(
    @DecimalMin("-180.0") @DecimalMax("180.0") double longitude,
    @DecimalMin("-90.0") @DecimalMax("90.0") double latitude,
    @Positive @DecimalMax("30.0") double radiusKilometers
) {
}
