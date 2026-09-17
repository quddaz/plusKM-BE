package personal_projects.backend.domain.emergency.dto.response;

import java.util.List;

public record EmergencyBedsResponse(List<EmergencyBedResponse> availabilities) {

    public EmergencyBedsResponse {
        availabilities = List.copyOf(availabilities);
    }

    public static EmergencyBedsResponse from(List<EmergencyBedResponse> availabilities) {
        return new EmergencyBedsResponse(availabilities);
    }
}
