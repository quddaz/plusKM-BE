package personal_projects.backend.domain.bookmark.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import personal_projects.backend.domain.bookmark.entity.Bookmark;
import personal_projects.backend.domain.place.entity.Place;
import personal_projects.backend.domain.user.entity.User;

import java.util.Optional;

public interface BookmarkJpaRepository extends JpaRepository<Bookmark, Long> {

    Optional<Bookmark> findByPlaceAndUser(Place place, User user);
}
