package personal_projects.backend.domain.emergency.dto.request;

import lombok.Builder;
@Builder
public record NearbyEmergencySearchRequest(
    double longitude,
    double latitude,
    double radiusKilometers
) {
}
