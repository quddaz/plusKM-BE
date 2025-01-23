package personal_projects.backend.domain.bookmark.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import personal_projects.backend.domain.bookmark.domain.BookMark;
import personal_projects.backend.domain.place.domain.Place;
import personal_projects.backend.domain.user.domain.User;

import java.util.Optional;

@Repository
public interface BookMarkRepository extends JpaRepository<BookMark, Long> {
    Optional<BookMark> findByPlaceAndUser(Place place, User user);

}
