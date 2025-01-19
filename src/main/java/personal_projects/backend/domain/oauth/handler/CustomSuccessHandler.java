package personal_projects.backend.domain.oauth.handler;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import personal_projects.backend.domain.oauth.dto.CustomOAuth2User;
import personal_projects.backend.domain.oauth.util.jwt.JwtTokenProvider;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request, HttpServletResponse response, Authentication authentication
    ) throws IOException {
        CustomOAuth2User authUser = (CustomOAuth2User) authentication.getPrincipal();

        String accessToken = jwtTokenProvider.createAccessToken(authUser.getUserId(), authUser.getRoles());
        Cookie refreshToken = jwtTokenProvider.createRefreshToken(authUser.getUserId(), authUser.getRoles());

        response.addCookie(refreshToken);
        response.setHeader("Authorization", "Bearer " + accessToken);

        response.sendRedirect("http://localhost:3000");
    }
}
