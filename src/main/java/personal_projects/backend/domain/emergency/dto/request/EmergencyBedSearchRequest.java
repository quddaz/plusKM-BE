package personal_projects.backend.domain.emergency.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record EmergencyBedSearchRequest(
    @NotEmpty
    @Size(max = 200)
    List<@NotBlank @Size(max = 20) String> hpids
) {
    public EmergencyBedSearchRequest {
        hpids = hpids == null ? null : List.copyOf(hpids);
    }
}
