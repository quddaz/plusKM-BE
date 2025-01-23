package personal_projects.backend.domain.medical.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import personal_projects.backend.domain.medical.domain.Medical;

@Repository
public interface MedicalRepository extends JpaRepository<Medical, Long> {
}
