package personal_projects.backend.domain.guardian.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record GuardianContactRequest(
    @NotBlank @Size(max = 40) String name,
    @NotBlank @Size(max = 20) String relationship,
    @NotBlank
    @Pattern(regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$", message = "올바른 휴대전화 번호가 아닙니다.")
    String phoneNumber
) {
}
