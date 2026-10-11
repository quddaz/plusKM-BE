package personal_projects.backend.domain.emergency.dto.response;

import lombok.Builder;
@Builder
public record NearbyEmergencyResponse(
    Long id,
    String hpid,
    String name,
    String address,
    String phoneNumber,
    double longitude,
    double latitude
) {
}
