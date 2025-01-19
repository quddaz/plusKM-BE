package personal_projects.backend.domain.oauth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    @GetMapping("/{id}")
    @Operation(summary = "테스트 토큰 발급", description = "userId를 받아 테스트 토큰을 발급합니다")
    public ResponseTemplate<?> test(@PathVariable(name = "id") long id) {
        return ResponseTemplate.from(authService.generateTestToken(id));
    }
}
