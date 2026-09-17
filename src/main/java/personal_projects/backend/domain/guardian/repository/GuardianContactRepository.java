package personal_projects.backend.domain.guardian.repository;

import java.util.List;
import java.util.Optional;
import personal_projects.backend.domain.guardian.entity.GuardianContact;

public interface GuardianContactRepository {

    GuardianContact save(GuardianContact guardianContact);

    List<GuardianContact> findAllByUserId(Long userId);

    Optional<GuardianContact> findByIdAndUserId(Long id, Long userId);

    void delete(GuardianContact guardianContact);
}
