package personal_projects.backend.domain.oauth.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import personal_projects.backend.domain.oauth.service.AuthService;
import personal_projects.backend.global.dto.ResponseTemplate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @GetMapping("/reissue")
    public ResponseEntity<ResponseTemplate<?>> reIssueToken(
        @CookieValue(name = "REFRESH_TOKEN", required = false) String refreshToken, HttpServletResponse response) {

        authService.reIssueToken(refreshToken, response);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ResponseTemplate.EMPTY_RESPONSE);
    }

    @Operation(summary = "테스트 토큰 발급", description = "userId를 받아 테스트 토큰을 발급합니다")
    @GetMapping("/test/{userId}")
    public String test(@PathVariable Long userId) {
        return authService.generateTestToken(userId);
    }
}
