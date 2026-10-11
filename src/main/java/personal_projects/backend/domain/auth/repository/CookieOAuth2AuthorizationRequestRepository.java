package personal_projects.backend.domain.auth.repository;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

@Component
public class CookieOAuth2AuthorizationRequestRepository
    implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String COOKIE_NAME = "pluskm_oauth_request";
    private static final int COOKIE_MAX_AGE_SECONDS = 180;
    private final byte[] signingKey;

    @Autowired
    public CookieOAuth2AuthorizationRequestRepository(@Value("${jwt.secret-key}") String signingKey) {
        this.signingKey = signingKey.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        String value = findCookie(request);
        if (value == null) {
            return null;
        }
        try {
            String[] parts = value.split("\\.", 2);
            if (parts.length != 2 || !MessageDigest.isEqual(sign(parts[0]), decode(parts[1]))) {
                return null;
            }
            try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(decode(parts[0])))) {
                return (OAuth2AuthorizationRequest) input.readObject();
            }
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public void saveAuthorizationRequest(
        OAuth2AuthorizationRequest authorizationRequest,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        if (authorizationRequest == null) {
            deleteCookie(response);
            return;
        }
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
                output.writeObject(authorizationRequest);
            }
            String payload = encode(bytes.toByteArray());
            addCookie(response, payload + "." + encode(sign(payload)), COOKIE_MAX_AGE_SECONDS);
        } catch (Exception exception) {
            throw new IllegalStateException("OAuth 인증 요청을 쿠키에 저장하지 못했습니다.", exception);
        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
        deleteCookie(response);
        return authorizationRequest;
    }

    private String findCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private byte[] sign(String payload) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(signingKey, "HmacSHA256"));
        return mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
    }

    private String encode(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private byte[] decode(String value) {
        return Base64.getUrlDecoder().decode(value);
    }

    private void addCookie(HttpServletResponse response, String value, int maxAge) {
        response.addHeader("Set-Cookie", ResponseCookie.from(COOKIE_NAME, value)
            .httpOnly(true)
            .secure(true)
            .sameSite("Lax")
            .path("/")
            .maxAge(maxAge)
            .build()
            .toString());
    }

    private void deleteCookie(HttpServletResponse response) {
        addCookie(response, "", 0);
    }
}
