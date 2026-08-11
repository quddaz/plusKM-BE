package personal_projects.backend.domain.user.repository.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import personal_projects.backend.domain.user.domain.User;
import personal_projects.backend.domain.user.repository.UserRepository;
import personal_projects.backend.domain.user.repository.jpa.UserJpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;

    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<User> findBySocialId(String socialId) {
        return jpaRepository.findBySocialId(socialId);
    }

    @Override
    public User save(User user) {
        return jpaRepository.save(user);
    }

    @Override
    public List<User> saveAll(Iterable<User> users) {
        return jpaRepository.saveAll(users);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }
}
