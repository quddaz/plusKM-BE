package personal_projects.backend.domain.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
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

}
