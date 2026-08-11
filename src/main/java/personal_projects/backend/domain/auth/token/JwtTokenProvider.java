package personal_projects.backend.domain.auth.token;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import personal_projects.backend.domain.user.entity.User;
import personal_projects.backend.domain.user.service.UserService;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtTokenProvider {

    private Key signingKey;
    private static final String ROLE_CLAIM = "role";

    private final JwtProperties jwtProperties;
    private final UserService userService;

    @PostConstruct
    public void initializeSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.secretKey());
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * AccessToken 생성 메소드
     */
    public String createAccessToken(Long userId, List<String> roles) {
        long issuedAtMillis = System.currentTimeMillis();

        Date expiresAt = new Date(issuedAtMillis + jwtProperties.accessTokenExpiration());

        return Jwts.builder()
            .setIssuedAt(new Date(issuedAtMillis))
            .setExpiration(expiresAt)
            .setIssuer(jwtProperties.issuer())
            .setSubject(userId.toString())
            .addClaims(Map.of(ROLE_CLAIM, roles))
            .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
            .signWith(signingKey, SignatureAlgorithm.HS512)
            .compact();
    }
    /**
     * RefreshToken 생성
     */
    public Cookie createRefreshTokenCookie(Long userId, List<String> roles) {
        long issuedAtMillis = System.currentTimeMillis();

        // Refresh token 유효 기간 설정
        Date expiresAt = new Date(issuedAtMillis + jwtProperties.refreshTokenExpiration());

        // Refresh token 생성
        String refreshToken = Jwts.builder()
            .setIssuedAt(new Date(issuedAtMillis))
            .setExpiration(expiresAt)
            .setIssuer(jwtProperties.issuer())
            .setSubject(userId.toString())
            .addClaims(Map.of(ROLE_CLAIM, roles))
            .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
            .signWith(signingKey, SignatureAlgorithm.HS512)
            .compact();

        return buildRefreshTokenCookie(refreshToken);
    }

    /**
     * 토큰 유효성 검사
     */
    public boolean validateToken(final String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token);
            return claims.getBody().getExpiration().after(new Date());
        } catch (Exception e) {
            log.warn("JWT validation failed");
            return false;
        }
    }

    public User resolveUser(String token) {
        Long userId = Long.parseLong(Jwts.parserBuilder().setSigningKey(signingKey).build()
            .parseClaimsJws(token).getBody().getSubject());

        return userService.findById(userId);
    }

    /**
     * 일반 Cookie 생성
     */
    private Cookie buildRefreshTokenCookie(String refreshToken) {
        Cookie cookie = new Cookie("REFRESH_TOKEN", refreshToken);
        cookie.setMaxAge((int) (jwtProperties.refreshTokenExpiration() / 1000)); // 초 단위로 설정
        cookie.setPath("/");
        cookie.setSecure(true); // HTTPS에서만 전송
        cookie.setHttpOnly(true); // JavaScript에서 접근 불가
        return cookie;
    }
}
