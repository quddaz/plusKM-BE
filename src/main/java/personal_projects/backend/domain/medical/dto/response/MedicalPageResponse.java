package personal_projects.backend.domain.medical.dto.response;

import lombok.Builder;

@Builder
public record MedicalPageResponse(
    Long id,
    String content,
    String department,
    Long fee,
    String name
) {
}
