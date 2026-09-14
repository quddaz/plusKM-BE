package personal_projects.backend.domain.emergency.repository;

import personal_projects.backend.domain.emergency.entity.Emergency;
import personal_projects.backend.domain.emergency.type.EmergencySearchType;
import personal_projects.backend.domain.emergency.dto.response.EmergencyDetailResponse;
import personal_projects.backend.domain.emergency.dto.response.NearbyEmergencyResponse;

import java.util.List;
import java.util.Optional;

public interface EmergencyRepository {

    Optional<Emergency> findById(Long id);

    Optional<Emergency> findByHpid(String hpid);

    List<Emergency> findAllActive();

    void saveAll(List<Emergency> emergencies);

    List<NearbyEmergencyResponse> findNearbyEmergencies(
        double longitude,
        double latitude,
        double radiusKilometers,
        EmergencySearchType searchType
    );

    EmergencyDetailResponse findEmergencyDetail(Long emergencyId);
}
