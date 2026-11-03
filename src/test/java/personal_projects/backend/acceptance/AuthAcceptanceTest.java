package personal_projects.backend.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import personal_projects.backend.domain.user.type.Role;
import personal_projects.backend.support.ApiAcceptanceTest;

class AuthAcceptanceTest extends ApiAcceptanceTest {

    @ParameterizedTest
    @ValueSource(strings = {"/favorites", "/guardians"})
    void A01_개인정보_조회는_토큰이_없거나_유효하지_않으면_401이다(String path) {
        assertThat(call(GET, path, null, null).getStatusCode().value()).isEqualTo(401);
        assertThat(call(GET, path, "Bearer invalid-token", null).getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void A02_탈퇴한_사용자의_기존_토큰은_500이_아닌_401이다() {
        var owner = user(Role.USER);
        String token = bearer(owner);
        users.deleteById(owner.getId());

        assertThat(call(GET, "/favorites", token, null).getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void A03_로컬이_아닌_환경에서는_사용자_ID만으로_토큰을_발급하지_않는다() {
        var owner = user(Role.USER);
        assertThat(call(GET, "/auth/test-tokens/" + owner.getId(), null, null).getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void A04_리프레시_쿠키가_유효하면_액세스토큰과_보안_쿠키를_반환한다() {
        var owner = user(Role.USER);
        var cookie = tokens.createRefreshTokenCookie(owner.getId(), owner.roleAuthorities());
        var headers = new HttpHeaders();
        headers.set(HttpHeaders.COOKIE, cookie.getName() + "=" + cookie.getValue());

        var result = http.exchange("/auth/reissue", HttpMethod.POST, new HttpEntity<>(null, headers), JsonNode.class);

        assertThat(result.getStatusCode().value()).isEqualTo(204);
        assertThat(result.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).startsWith("Bearer ");
        assertThat(result.getHeaders().getFirst(HttpHeaders.SET_COOKIE)).contains("HttpOnly", "Secure", "REFRESH_TOKEN=");
        assertThat(result.getHeaders().getFirst(HttpHeaders.SET_COOKIE)).doesNotContain("JSESSIONID");
        assertThat(call(POST, "/auth/reissue", null, null).getStatusCode().value()).isEqualTo(401);
    }
}
