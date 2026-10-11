package personal_projects.backend.domain.emergency.client;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmergencyBedApiClient {

    private final EmergencyApiRequester requester;
    private final EmergencyBedApiResponseParser parser;

    public List<EmergencyBedData> fetchBeds() {
        return parser.parse(requester.requestBeds());
    }
}
