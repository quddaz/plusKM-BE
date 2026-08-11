package personal_projects.backend.domain.medical.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import personal_projects.backend.domain.medical.domain.Medical;

public interface MedicalJpaRepository extends JpaRepository<Medical, Long> {
}
