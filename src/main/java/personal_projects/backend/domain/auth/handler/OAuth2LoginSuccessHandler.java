package personal_projects.backend.domain.auth.handler;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import personal_projects.backend.domain.auth.dto.AuthenticatedUserPrincipal;
import personal_projects.backend.domain.auth.token.JwtTokenProvider;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request, HttpServletResponse response, Authentication authentication
    ) throws IOException {
        AuthenticatedUserPrincipal principal = (AuthenticatedUserPrincipal) authentication.getPrincipal();

        String accessToken = jwtTokenProvider.createAccessToken(principal.getUserId(), principal.getRoles());
        Cookie refreshToken = jwtTokenProvider.createRefreshTokenCookie(principal.getUserId(), principal.getRoles());

        response.addCookie(refreshToken);
        response.setHeader("Authorization", "Bearer " + accessToken);

        response.sendRedirect("http://localhost:3000");
    }
}
