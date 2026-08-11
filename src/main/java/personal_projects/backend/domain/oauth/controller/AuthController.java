package personal_projects.backend.domain.oauth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import personal_projects.backend.domain.oauth.dto.response.TokenResponse;
import personal_projects.backend.domain.oauth.service.AuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/reissue")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reIssueToken(
        @CookieValue(name = "REFRESH_TOKEN", required = false) String refreshToken, HttpServletResponse response) {
        authService.reIssueToken(refreshToken, response);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "테스트 토큰 발급", description = "userId를 받아 테스트 토큰을 발급합니다")
    public TokenResponse test(@PathVariable(name = "id") long id) {
        return TokenResponse.from(authService.generateTestToken(id));
    }
}
