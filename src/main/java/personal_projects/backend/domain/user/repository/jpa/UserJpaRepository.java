package personal_projects.backend.domain.user.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import personal_projects.backend.domain.user.entity.User;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<User, Long> {

    Optional<User> findBySocialId(String socialId);
}
