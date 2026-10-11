package personal_projects.backend.domain.guardian.repository.adapter;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import personal_projects.backend.domain.guardian.entity.GuardianContact;
import personal_projects.backend.domain.guardian.repository.GuardianContactRepository;
import personal_projects.backend.domain.guardian.repository.jpa.GuardianContactJpaRepository;

@Repository
@RequiredArgsConstructor
public class GuardianContactRepositoryAdapter implements GuardianContactRepository {

    private final GuardianContactJpaRepository jpaRepository;

    @Override
    public GuardianContact save(GuardianContact guardianContact) {
        return jpaRepository.save(guardianContact);
    }

    @Override
    public List<GuardianContact> findAllByUserId(Long userId) {
        return jpaRepository.findAllByUserIdOrderByIdAsc(userId);
    }

    @Override
    public Optional<GuardianContact> findByIdAndUserId(Long id, Long userId) {
        return jpaRepository.findByIdAndUserId(id, userId);
    }

    @Override
    public void delete(GuardianContact guardianContact) {
        jpaRepository.delete(guardianContact);
    }
}
