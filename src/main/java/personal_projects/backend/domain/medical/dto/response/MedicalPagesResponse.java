package personal_projects.backend.domain.medical.dto.response;

import java.util.List;

public record MedicalPagesResponse(List<MedicalPageResponse> medicals) {

    public MedicalPagesResponse {
        medicals = List.copyOf(medicals);
    }

    public static MedicalPagesResponse from(List<MedicalPageResponse> medicals) {
        return new MedicalPagesResponse(medicals);
    }
}
