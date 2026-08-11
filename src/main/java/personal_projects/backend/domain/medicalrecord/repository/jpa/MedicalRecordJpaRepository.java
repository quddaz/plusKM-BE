package personal_projects.backend.domain.medicalrecord.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import personal_projects.backend.domain.medicalrecord.entity.MedicalRecord;

public interface MedicalRecordJpaRepository extends JpaRepository<MedicalRecord, Long> {
}
