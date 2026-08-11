package personal_projects.backend.domain.user.repository;

import personal_projects.backend.domain.user.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findById(Long id);

    Optional<User> findBySocialId(String socialId);

    User save(User user);

    List<User> saveAll(Iterable<User> users);

    long count();
}
