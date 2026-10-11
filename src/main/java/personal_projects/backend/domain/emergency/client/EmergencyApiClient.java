package personal_projects.backend.domain.emergency.client;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmergencyApiClient {
    private final EmergencyApiRequester requester;
    private final EmergencyApiResponseParser parser;

    public List<EmergencyFacilityData> fetchFacilities() {
        return parser.parse(requester.requestFacilities());
    }
}
