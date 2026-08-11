package personal_projects.backend.domain.medicalrecord.repository;

import personal_projects.backend.domain.medicalrecord.entity.MedicalRecord;
import personal_projects.backend.domain.medicalrecord.dto.response.MedicalRecordSummaryResponse;

import java.util.List;
import java.util.Optional;

public interface MedicalRecordRepository {

    MedicalRecord save(MedicalRecord medicalRecord);

    Optional<MedicalRecord> findById(Long id);

    void deleteById(Long id);

    List<MedicalRecordSummaryResponse> findMedicalRecords(
        String department,
        Long placeId,
        Long lastId,
        int size
    );
}
