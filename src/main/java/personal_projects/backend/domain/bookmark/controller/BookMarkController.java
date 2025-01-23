package personal_projects.backend.domain.bookmark.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import personal_projects.backend.domain.bookmark.service.BookMarkService;
import personal_projects.backend.domain.oauth.dto.CustomOAuth2User;
import personal_projects.backend.global.dto.ResponseTemplate;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/bookmark")
public class BookMarkController {
    private final BookMarkService bookMarkService;

    @GetMapping("/check/{id}")
    public ResponseEntity<ResponseTemplate<?>> checkBookMark(@PathVariable Long id,
                                                             @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        bookMarkService.checkBookMark(id, customOAuth2User.getUserId());
        return ResponseEntity.ok(ResponseTemplate.EMPTY_RESPONSE);
    }
}
