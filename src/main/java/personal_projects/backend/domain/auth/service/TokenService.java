package personal_projects.backend.domain.auth.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import personal_projects.backend.domain.auth.exception.InvalidRefreshTokenException;
import personal_projects.backend.domain.auth.exception.code.AuthErrorCode;
import personal_projects.backend.domain.auth.token.JwtTokenProvider;
import personal_projects.backend.domain.user.entity.User;
import personal_projects.backend.domain.user.type.Role;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;

    public void reissueAccessToken(String refreshToken, HttpServletResponse response) {

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new InvalidRefreshTokenException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        User user = jwtTokenProvider.resolveUser(refreshToken);

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.roleAuthorities());
        Cookie refreshTokenCookie = jwtTokenProvider.createRefreshTokenCookie(user.getId(), user.roleAuthorities());

        response.addCookie(refreshTokenCookie);
        response.setHeader("Authorization", "Bearer " + accessToken);
    }

    public String createTestAccessToken(Long userId) {
        return jwtTokenProvider.createAccessToken(userId, List.of(Role.USER.authority()));
    }
}
