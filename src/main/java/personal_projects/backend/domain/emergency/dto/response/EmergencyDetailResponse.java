package personal_projects.backend.domain.emergency.dto.response;

import lombok.Builder;

@Builder
public record EmergencyDetailResponse(
    Long id,
    String name,
    String address,
    String phoneNumber
) {
}
