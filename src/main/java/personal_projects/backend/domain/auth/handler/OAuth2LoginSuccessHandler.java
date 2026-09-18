package personal_projects.backend.domain.auth.handler;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import personal_projects.backend.domain.auth.dto.AuthenticatedUserPrincipal;
import personal_projects.backend.domain.auth.service.OAuth2LoginUserService;
import personal_projects.backend.domain.auth.token.JwtTokenProvider;
import personal_projects.backend.domain.user.entity.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final OAuth2LoginUserService oauth2LoginUserService;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request, HttpServletResponse response, Authentication authentication
    ) throws IOException {
        AuthenticatedUserPrincipal principal = resolvePrincipal(authentication.getPrincipal());

        String accessToken = jwtTokenProvider.createAccessToken(principal.getUserId(), principal.getRoles());
        Cookie refreshToken = jwtTokenProvider.createRefreshTokenCookie(principal.getUserId(), principal.getRoles());

        response.addCookie(refreshToken);
        response.setHeader("Authorization", "Bearer " + accessToken);

        // 액세스 토큰은 서버 로그와 Referer에 남지 않도록 URL fragment로 전달한다.
        response.sendRedirect(frontendUrl + "/#accessToken=" + accessToken);
    }

    private AuthenticatedUserPrincipal resolvePrincipal(Object authenticationPrincipal) {
        if (authenticationPrincipal instanceof AuthenticatedUserPrincipal principal) {
            return principal;
        }
        if (authenticationPrincipal instanceof OAuth2User oauth2User) {
            User user = oauth2LoginUserService.findOrCreateGoogleUser(oauth2User.getAttributes());
            return AuthenticatedUserPrincipal.fromUser(user);
        }
        throw new IllegalStateException("지원하지 않는 OAuth 사용자 형식입니다.");
    }
}
