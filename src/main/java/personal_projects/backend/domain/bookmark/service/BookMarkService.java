package personal_projects.backend.domain.bookmark.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import personal_projects.backend.domain.bookmark.domain.BookMark;
import personal_projects.backend.domain.bookmark.exception.BookMarkNotFoundException;
import personal_projects.backend.domain.bookmark.exception.errorCode.BookMarkErrorCode;
import personal_projects.backend.domain.bookmark.repository.BookMarkRepository;
import personal_projects.backend.domain.place.domain.Place;
import personal_projects.backend.domain.place.service.PlaceService;
import personal_projects.backend.domain.user.domain.User;
import personal_projects.backend.domain.user.service.UserService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookMarkService {

    private final BookMarkRepository bookMarkRepository;
    private final UserService userService;
    private final PlaceService placeService;

    public BookMark findById(Long id) {
        return bookMarkRepository.findById(id).orElseThrow(() -> new BookMarkNotFoundException(BookMarkErrorCode.BOOKMARK_NOT_FOUND));
    }

    @Transactional
    public void checkBookMark(Long placeId, Long userId) {
        User user = userService.findById(userId);
        Place place = placeService.findById(placeId);

        bookMarkRepository.findByPlaceAndUser(place, user).ifPresentOrElse(
                bookMark -> {
                    bookMarkRepository.delete(bookMark);
                },
                () -> {
                    bookMarkRepository.save(BookMark.of(place, user));
                }
        );
    }
}
