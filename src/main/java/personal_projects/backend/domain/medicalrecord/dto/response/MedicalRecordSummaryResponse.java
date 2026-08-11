package personal_projects.backend.domain.medicalrecord.dto.response;

import lombok.Builder;

@Builder
public record MedicalRecordSummaryResponse(
    Long id,
    String description,
    String department,
    Long medicalFee,
    String userName
) {
}
