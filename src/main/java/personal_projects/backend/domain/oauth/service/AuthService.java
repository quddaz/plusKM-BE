package personal_projects.backend.domain.oauth.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import personal_projects.backend.domain.oauth.exception.TokenNotValidException;
import personal_projects.backend.domain.oauth.exception.errorcode.AuthErrorCode;
import personal_projects.backend.domain.oauth.util.jwt.JwtTokenProvider;
import personal_projects.backend.domain.user.domain.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;

    public void reIssueToken(String refreshToken, HttpServletResponse response) {

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new TokenNotValidException(AuthErrorCode.TOKEN_NOT_VALID);
        }

        User user = jwtTokenProvider.getUser(refreshToken);

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRoles());
        Cookie newRefreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getRoles());

        response.addCookie(newRefreshToken);
        response.setHeader("Authorization", "Bearer " + accessToken);
    }

    public String generateTestToken(Long userId) {
        return jwtTokenProvider.createAccessToken(userId, List.of("Role_User"));
    }
}
