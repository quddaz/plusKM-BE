package personal_projects.backend.domain.emergency.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import personal_projects.backend.domain.emergency.entity.Emergency;

import java.util.List;

public interface EmergencyJpaRepository extends JpaRepository<Emergency, Long> {

    List<Emergency> findAllByActiveTrue();
}
