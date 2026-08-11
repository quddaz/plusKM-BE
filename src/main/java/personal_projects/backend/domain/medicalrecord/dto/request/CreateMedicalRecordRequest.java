package personal_projects.backend.domain.medicalrecord.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import personal_projects.backend.domain.medicalrecord.entity.MedicalRecord;
import personal_projects.backend.domain.medicalrecord.type.MedicalDepartment;
import personal_projects.backend.domain.place.entity.Place;
import personal_projects.backend.domain.user.entity.User;

@Builder
public record CreateMedicalRecordRequest(
    @NotEmpty(message = "진료 내용은 필수 값입니다. ")
    String description,
    @NotEmpty(message = "진료 과목은 필수 값입니다. ")
    String department,
    @NotNull(message = "진료 비용은 필수 값입니다.")
    Long medicalFee,
    @NotNull(message = "장소 ID는 필수 값입니다.")
    Long placeId
) {
    public MedicalRecord toEntity(Place place, User user) {
        return MedicalRecord.builder()
            .description(description)
            .department(MedicalDepartment.valueOf(department))
            .medicalFee(medicalFee)
            .place(place)
            .user(user)
            .build();
    }
}
