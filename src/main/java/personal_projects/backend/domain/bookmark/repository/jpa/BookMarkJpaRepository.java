package personal_projects.backend.domain.bookmark.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import personal_projects.backend.domain.bookmark.domain.BookMark;
import personal_projects.backend.domain.place.domain.Place;
import personal_projects.backend.domain.user.domain.User;

import java.util.Optional;

public interface BookMarkJpaRepository extends JpaRepository<BookMark, Long> {

    Optional<BookMark> findByPlaceAndUser(Place place, User user);
}
