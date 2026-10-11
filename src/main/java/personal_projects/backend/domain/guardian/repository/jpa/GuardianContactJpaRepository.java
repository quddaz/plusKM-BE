package personal_projects.backend.domain.guardian.repository.jpa;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import personal_projects.backend.domain.guardian.entity.GuardianContact;

public interface GuardianContactJpaRepository extends JpaRepository<GuardianContact, Long> {

    List<GuardianContact> findAllByUserIdOrderByIdAsc(Long userId);

    Optional<GuardianContact> findByIdAndUserId(Long id, Long userId);
}
