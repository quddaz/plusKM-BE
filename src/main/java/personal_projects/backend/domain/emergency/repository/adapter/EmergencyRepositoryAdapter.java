package personal_projects.backend.domain.emergency.repository.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import personal_projects.backend.domain.emergency.entity.Emergency;
import personal_projects.backend.domain.emergency.dto.response.EmergencyDetailResponse;
import personal_projects.backend.domain.emergency.dto.response.NearbyEmergencyResponse;
import personal_projects.backend.domain.emergency.repository.EmergencyRepository;
import personal_projects.backend.domain.emergency.repository.jdbc.EmergencyJdbcRepository;
import personal_projects.backend.domain.emergency.repository.jpa.EmergencyJpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EmergencyRepositoryAdapter implements EmergencyRepository {

    private final EmergencyJpaRepository jpaRepository;
    private final EmergencyJdbcRepository jdbcRepository;

    @Override
    public Optional<Emergency> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Emergency> findAllActive() {
        return jpaRepository.findAllByActiveTrue();
    }

    @Override
    public void saveAll(List<Emergency> emergencies) {
        jpaRepository.saveAll(emergencies);
    }

    @Override
    public List<NearbyEmergencyResponse> findNearbyEmergencies(
        double longitude,
        double latitude,
        double radiusKilometers
    ) {
        return jdbcRepository.findNearbyEmergencies(
            longitude,
            latitude,
            radiusKilometers
        );
    }

    @Override
    public EmergencyDetailResponse findEmergencyDetail(Long emergencyId) {
        return jdbcRepository.findEmergencyDetail(emergencyId);
    }
}
