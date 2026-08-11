package personal_projects.backend.domain.bookmark.repository.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import personal_projects.backend.domain.bookmark.entity.Bookmark;
import personal_projects.backend.domain.bookmark.repository.BookmarkRepository;
import personal_projects.backend.domain.bookmark.repository.jpa.BookmarkJpaRepository;
import personal_projects.backend.domain.place.entity.Place;
import personal_projects.backend.domain.user.entity.User;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BookmarkRepositoryAdapter implements BookmarkRepository {

    private final BookmarkJpaRepository jpaRepository;

    @Override
    public Optional<Bookmark> findByPlaceAndUser(Place place, User user) {
        return jpaRepository.findByPlaceAndUser(place, user);
    }

    @Override
    public Bookmark save(Bookmark bookmark) {
        return jpaRepository.save(bookmark);
    }

    @Override
    public void delete(Bookmark bookmark) {
        jpaRepository.delete(bookmark);
    }
}
