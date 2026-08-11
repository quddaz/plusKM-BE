package personal_projects.backend.domain.medicalrecord.dto.response;

import java.util.List;

public record MedicalRecordsResponse(List<MedicalRecordSummaryResponse> records) {

    public MedicalRecordsResponse {
        records = List.copyOf(records);
    }

    public static MedicalRecordsResponse from(List<MedicalRecordSummaryResponse> records) {
        return new MedicalRecordsResponse(records);
    }
}
