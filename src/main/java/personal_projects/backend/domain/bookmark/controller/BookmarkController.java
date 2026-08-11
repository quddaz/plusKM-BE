package personal_projects.backend.domain.bookmark.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import personal_projects.backend.domain.bookmark.service.BookmarkService;
import personal_projects.backend.domain.auth.dto.AuthenticatedUserPrincipal;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/bookmarks")
public class BookmarkController {
    private final BookmarkService bookmarkService;

    @PostMapping("/{id}/toggle")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void toggleBookmark(@PathVariable(name = "id") long placeId,
                               @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        bookmarkService.toggleBookmark(placeId, principal.getUserId());
    }
}
