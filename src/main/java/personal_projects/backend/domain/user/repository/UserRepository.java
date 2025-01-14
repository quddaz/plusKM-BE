package personal_projects.backend.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import personal_projects.backend.domain.user.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
