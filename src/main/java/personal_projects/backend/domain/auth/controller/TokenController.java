package personal_projects.backend.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import personal_projects.backend.domain.auth.dto.response.AccessTokenResponse;
import personal_projects.backend.domain.auth.service.TokenService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class TokenController {

    private final TokenService tokenService;

    @PostMapping("/reissue")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reissueAccessToken(
        @CookieValue(name = "REFRESH_TOKEN", required = false) String refreshToken, HttpServletResponse response) {
        tokenService.reissueAccessToken(refreshToken, response);
    }

    @GetMapping("/test-tokens/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "테스트 토큰 발급", description = "userId를 받아 테스트 토큰을 발급합니다")
    public AccessTokenResponse issueTestAccessToken(@PathVariable long userId) {
        return AccessTokenResponse.from(tokenService.createTestAccessToken(userId));
    }
}
