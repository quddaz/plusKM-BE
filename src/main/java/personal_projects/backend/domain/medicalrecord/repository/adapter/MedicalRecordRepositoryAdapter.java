package personal_projects.backend.domain.medicalrecord.repository.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import personal_projects.backend.domain.medicalrecord.entity.MedicalRecord;
import personal_projects.backend.domain.medicalrecord.dto.response.MedicalRecordSummaryResponse;
import personal_projects.backend.domain.medicalrecord.repository.MedicalRecordRepository;
import personal_projects.backend.domain.medicalrecord.repository.jdbc.MedicalRecordJdbcRepository;
import personal_projects.backend.domain.medicalrecord.repository.jpa.MedicalRecordJpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MedicalRecordRepositoryAdapter implements MedicalRecordRepository {

    private final MedicalRecordJpaRepository jpaRepository;
    private final MedicalRecordJdbcRepository jdbcRepository;

    @Override
    public MedicalRecord save(MedicalRecord medicalRecord) {
        return jpaRepository.save(medicalRecord);
    }

    @Override
    public Optional<MedicalRecord> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<MedicalRecordSummaryResponse> findMedicalRecords(
        String department,
        Long placeId,
        Long lastId,
        int size
    ) {
        return jdbcRepository.findMedicalRecords(department, placeId, lastId, size);
    }
}
