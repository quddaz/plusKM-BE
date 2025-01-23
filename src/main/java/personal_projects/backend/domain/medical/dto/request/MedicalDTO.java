package personal_projects.backend.domain.medical.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import personal_projects.backend.domain.medical.domain.Medical;
import personal_projects.backend.domain.medical.domain.Medical_department;
import personal_projects.backend.domain.place.domain.Place;
import personal_projects.backend.domain.user.domain.User;

@Builder
public record MedicalDTO(
    @NotEmpty(message = "진료 내용은 필수 값입니다. ")
    String content,
    @NotEmpty(message = "진료 과목은 필수 값입니다. ")
    String department,
    @NotEmpty(message = "진료 비용은 필수 값입니다. ")
    Long medical_fee,
    @NotEmpty(message = "장소 ID는 필수 값입니다. ")
    Long place_id
) {
    public static Medical toEntity(MedicalDTO medicalDTO, Place place, User user) {
        return Medical.builder()
            .content(medicalDTO.content())
            .department(Medical_department.valueOf(medicalDTO.department()))
            .medical_fee(medicalDTO.medical_fee())
            .place(place)
            .user(user)
            .build();
    }
}
