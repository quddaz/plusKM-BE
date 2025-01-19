package personal_projects.backend.domain.oauth.util.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import personal_projects.backend.domain.user.domain.User;
import personal_projects.backend.domain.user.service.UserService;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtTokenProvider {

    private Key key;
    private static final String MEMBER_ROLE = "role";

    private final JwtProperties jwtProperties;
    private final UserService userService;

    @PostConstruct
    public void setKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.secretKey());
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * AccessToken 생성 메소드
     */
    public String createAccessToken(Long userId, List<String> roles) {
        long now = (new Date()).getTime();

        Date accessValidity = new Date(now + jwtProperties.accessTokenExpiration());

        return Jwts.builder()
            .setIssuedAt(new Date(now))
            .setExpiration(accessValidity)
            .setIssuer(jwtProperties.issuer())
            .setSubject(userId.toString())
            .addClaims(Map.of(MEMBER_ROLE, roles))
            .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact();
    }
    /**
     * RefreshToken 생성
     */
    public Cookie createRefreshToken(Long userId, List<String> roles) {
        long now = (new Date()).getTime();

        // Refresh token 유효 기간 설정
        Date refreshValidity = new Date(now + jwtProperties.refreshTokenExpiration());

        // Refresh token 생성
        String refreshToken = Jwts.builder()
            .setIssuedAt(new Date(now))
            .setExpiration(refreshValidity)
            .setIssuer(jwtProperties.issuer())
            .setSubject(userId.toString())
            .addClaims(Map.of(MEMBER_ROLE, roles))
            .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact();

        return createCookie(refreshToken);
    }

    /**
     * 토큰 유효성 검사
     */
    public boolean validateToken(final String token) {
        try {
            log.info("now date: {}", new Date());
            Jws<Claims> claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return claims.getBody().getExpiration().after(new Date());
        } catch (Exception e) {
            log.error("Token validation error: ", e);
            return false;
        }
    }

    public User getUser(String token) {
        Long id = Long.parseLong(Jwts.parserBuilder().setSigningKey(key).build()
            .parseClaimsJws(token).getBody().getSubject());

        log.info("in getMember() socialId: {}", id);

        return userService.findById(id);
    }

    /**
     * 일반 Cookie 생성
     */
    public Cookie createCookie(String refreshToken) {
        Cookie cookie = new Cookie("REFRESH_TOKEN", refreshToken);
        cookie.setMaxAge((int) (jwtProperties.refreshTokenExpiration() / 1000)); // 초 단위로 설정
        cookie.setPath("/");
        cookie.setSecure(true); // HTTPS에서만 전송
        cookie.setHttpOnly(true); // JavaScript에서 접근 불가
        return cookie;
    }
}
