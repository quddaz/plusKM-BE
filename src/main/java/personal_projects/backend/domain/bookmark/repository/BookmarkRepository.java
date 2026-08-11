package personal_projects.backend.domain.bookmark.repository;

import personal_projects.backend.domain.bookmark.entity.Bookmark;
import personal_projects.backend.domain.place.entity.Place;
import personal_projects.backend.domain.user.entity.User;

import java.util.Optional;

public interface BookmarkRepository {

    Optional<Bookmark> findByPlaceAndUser(Place place, User user);

    Bookmark save(Bookmark bookmark);

    void delete(Bookmark bookmark);
}
