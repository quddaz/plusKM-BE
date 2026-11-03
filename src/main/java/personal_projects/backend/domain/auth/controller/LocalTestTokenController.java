package personal_projects.backend.domain.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import personal_projects.backend.domain.auth.dto.response.AccessTokenResponse;
import personal_projects.backend.domain.auth.service.TokenService;

@Profile("local")
@RestController
@RequiredArgsConstructor
public class LocalTestTokenController {

    private final TokenService tokenService;

    @GetMapping("/auth/test-tokens/{userId}")
    public AccessTokenResponse issueTestAccessToken(@PathVariable long userId) {
        return AccessTokenResponse.from(tokenService.createTestAccessToken(userId));
    }
}
