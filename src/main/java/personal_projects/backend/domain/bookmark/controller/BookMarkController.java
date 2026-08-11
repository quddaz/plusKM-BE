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
import personal_projects.backend.domain.bookmark.service.BookMarkService;
import personal_projects.backend.domain.oauth.dto.CustomOAuth2User;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/bookmark")
public class BookMarkController {
    private final BookMarkService bookMarkService;

    @PostMapping("/{id}/toggle")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void checkBookMark(@PathVariable(name = "id") long id,
                              @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        bookMarkService.checkBookMark(id, customOAuth2User.getUserId());
    }
}
