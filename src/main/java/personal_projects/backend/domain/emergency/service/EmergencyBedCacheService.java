package personal_projects.backend.domain.emergency.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import personal_projects.backend.domain.emergency.cache.EmergencyBedCache;
import personal_projects.backend.domain.emergency.client.EmergencyBedApiClient;
import personal_projects.backend.domain.emergency.client.EmergencyBedData;
import personal_projects.backend.domain.emergency.dto.response.EmergencyBedResponse;

@Service
@RequiredArgsConstructor
public class EmergencyBedCacheService {

    private final EmergencyBedApiClient apiClient;
    private final EmergencyBedCache cache;

    public void refresh() {
        List<EmergencyBedData> beds = apiClient.fetchBeds();
        if (beds.isEmpty()) {
            throw new IllegalStateException("응급실 병상 API가 빈 목록을 반환했습니다.");
        }
        cache.replaceAll(beds);
    }

    public List<EmergencyBedResponse> findBeds(List<String> hpids) {
        return cache.findAll(hpids).values().stream()
            .map(EmergencyBedResponse::from)
            .toList();
    }
}
