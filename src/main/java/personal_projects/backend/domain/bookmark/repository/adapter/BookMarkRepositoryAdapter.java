package personal_projects.backend.domain.bookmark.repository.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import personal_projects.backend.domain.bookmark.domain.BookMark;
import personal_projects.backend.domain.bookmark.repository.BookMarkRepository;
import personal_projects.backend.domain.bookmark.repository.jpa.BookMarkJpaRepository;
import personal_projects.backend.domain.place.domain.Place;
import personal_projects.backend.domain.user.domain.User;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BookMarkRepositoryAdapter implements BookMarkRepository {

    private final BookMarkJpaRepository jpaRepository;

    @Override
    public Optional<BookMark> findByPlaceAndUser(Place place, User user) {
        return jpaRepository.findByPlaceAndUser(place, user);
    }

    @Override
    public BookMark save(BookMark bookMark) {
        return jpaRepository.save(bookMark);
    }

    @Override
    public void delete(BookMark bookMark) {
        jpaRepository.delete(bookMark);
    }
}
