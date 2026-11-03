package personal_projects.backend.support;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.http.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;

@Tag("acceptance")
public abstract class ApiAcceptanceTest extends MySqlIntegrationTest {

    @Autowired protected TestRestTemplate http;

    @BeforeEach
    void configureHttpClient() {
        // 401 응답을 자동 인증 재시도 없이 그대로 검사한다.
        http.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory(
            HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build()));
    }

    protected ResponseEntity<JsonNode> call(HttpMethod method, String path, String bearer, Object body) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (bearer != null) headers.set(HttpHeaders.AUTHORIZATION, bearer);
        return http.exchange(path, method, new HttpEntity<>(body, headers), JsonNode.class);
    }
}
