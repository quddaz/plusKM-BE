package personal_projects.backend.domain.emergency.service;

import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import personal_projects.backend.domain.emergency.entity.Emergency;
import personal_projects.backend.domain.emergency.dto.request.NearbyEmergencySearchRequest;
import personal_projects.backend.domain.emergency.dto.response.EmergencyDetailResponse;
import personal_projects.backend.domain.emergency.dto.response.NearbyEmergencyResponse;
import personal_projects.backend.domain.emergency.repository.EmergencyRepository;

import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class EmergencyService {

    private final EmergencyRepository emergencyRepository;

    public Emergency findById(Long id) {
        return emergencyRepository.findById(id).orElseThrow(() ->
            new NoSuchElementException("응급 정보를 찾을 수 없습니다."));
    }

    public List<NearbyEmergencyResponse> findNearbyEmergencies(NearbyEmergencySearchRequest request) {
        return emergencyRepository.findNearbyEmergencies(
            request.longitude(),
            request.latitude(),
            request.radiusKilometers()
        );
    }

    public EmergencyDetailResponse findEmergencyDetail(Long emergencyId) {
        return emergencyRepository.findEmergencyDetail(emergencyId);
    }
}
