package personal_projects.backend.domain.bookmark.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import personal_projects.backend.domain.bookmark.entity.Bookmark;
import personal_projects.backend.domain.bookmark.repository.BookmarkRepository;
import personal_projects.backend.domain.place.entity.Place;
import personal_projects.backend.domain.place.service.PlaceService;
import personal_projects.backend.domain.user.entity.User;
import personal_projects.backend.domain.user.service.UserService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final UserService userService;
    private final PlaceService placeService;

    @Transactional
    public void toggleBookmark(Long placeId, Long userId) {
        User user = userService.findById(userId);
        Place place = placeService.findById(placeId);

        bookmarkRepository.findByPlaceAndUser(place, user).ifPresentOrElse(
                bookmarkRepository::delete,
                () -> {
                    bookmarkRepository.save(Bookmark.of(place, user));
                }
        );
    }
}
