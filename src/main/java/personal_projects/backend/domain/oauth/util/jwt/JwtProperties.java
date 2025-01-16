package personal_projects.backend.oauth.util.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
    String secretKey,
    String issuer,
    Long accessTokenExpiration,
    Long refreshTokenExpiration) {

}