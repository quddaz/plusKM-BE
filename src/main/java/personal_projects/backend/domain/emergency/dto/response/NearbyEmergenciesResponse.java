package personal_projects.backend.domain.emergency.dto.response;

import java.util.List;

public record NearbyEmergenciesResponse(List<NearbyEmergencyResponse> emergencies) {

    public NearbyEmergenciesResponse {
        emergencies = List.copyOf(emergencies);
    }

    public static NearbyEmergenciesResponse from(List<NearbyEmergencyResponse> emergencies) {
        return new NearbyEmergenciesResponse(emergencies);
    }
}
