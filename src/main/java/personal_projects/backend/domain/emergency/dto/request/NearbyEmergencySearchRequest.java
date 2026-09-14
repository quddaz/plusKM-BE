package personal_projects.backend.domain.emergency.dto.request;

import lombok.Builder;
import personal_projects.backend.domain.emergency.type.EmergencySearchType;

@Builder
public record NearbyEmergencySearchRequest(
    double longitude,
    double latitude,
    double radiusKilometers,
    EmergencySearchType searchType
) {
}
