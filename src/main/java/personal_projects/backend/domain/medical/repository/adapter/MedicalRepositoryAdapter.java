package personal_projects.backend.domain.medical.repository.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import personal_projects.backend.domain.medical.domain.Medical;
import personal_projects.backend.domain.medical.dto.response.MedicalPageResponse;
import personal_projects.backend.domain.medical.repository.MedicalRepository;
import personal_projects.backend.domain.medical.repository.jdbc.MedicalJdbcRepository;
import personal_projects.backend.domain.medical.repository.jpa.MedicalJpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MedicalRepositoryAdapter implements MedicalRepository {

    private final MedicalJpaRepository jpaRepository;
    private final MedicalJdbcRepository jdbcRepository;

    @Override
    public Medical save(Medical medical) {
        return jpaRepository.save(medical);
    }

    @Override
    public Optional<Medical> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<MedicalPageResponse> findMedicalByDepartment(
        String department,
        Long placeId,
        Long lastId,
        int size
    ) {
        return jdbcRepository.findMedicalByDepartment(department, placeId, lastId, size);
    }
}
