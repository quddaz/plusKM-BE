package personal_projects.backend.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import java.util.Map;
import org.junit.jupiter.api.Test;
import personal_projects.backend.domain.user.type.Role;
import personal_projects.backend.support.ApiAcceptanceTest;

class GuardianAcceptanceTest extends ApiAcceptanceTest {

    @Test
    void G01_보호자_등록과_마스킹_조회와_문자_앱_링크_생성을_검증한다() {
        var owner = user(Role.USER);
        emergency("A0001", true, 127.1, 37.4);
        var created = call(POST, "/guardians", bearer(owner), contact("010-1234-5678"));
        assertThat(created.getStatusCode().value()).isEqualTo(201);
        long id = created.getBody().path("id").asLong();
        assertThat(created.getBody().path("maskedPhoneNumber").asText()).isEqualTo("010-****-5678");
        assertThat(call(GET, "/guardians", bearer(owner), null).getBody()).hasSize(1);
        var message = call(POST, "/guardians/" + id + "/emergency-message", bearer(owner), Map.of(
            "emergencyHpid", "A0001", "longitude", 127.1, "latitude", 37.4));
        assertThat(message.getStatusCode().value()).isEqualTo(200);
        assertThat(message.getBody().path("smsUri").asText()).startsWith("sms:01012345678?body=");
        assertThat(message.getBody().path("message").asText()).contains("테스트 사용자", "A0001 응급실");
        assertThat(call(DELETE, "/guardians/" + id, bearer(owner), null).getStatusCode().value()).isEqualTo(204);
        assertThat(call(GET, "/guardians", bearer(owner), null).getBody()).isEmpty();
    }

    @Test
    void G02_남의_보호자_수정과_삭제와_알림은_404이며_비활성_알림은_400이다() {
        var owner = user(Role.USER);
        var other = user(Role.USER);
        long id = call(POST, "/guardians", bearer(owner), contact("01012345678")).getBody().path("id").asLong();
        var updated = Map.of("name", "이름변경", "relationship", "친구", "phoneNumber", "01011112222", "active", false);
        var message = Map.of("emergencyHpid", "A0001", "longitude", 127.1, "latitude", 37.4);
        String path = "/guardians/" + id;

        assertThat(call(PUT, path, bearer(other), updated).getStatusCode().value()).isEqualTo(404);
        assertThat(call(DELETE, path, bearer(other), null).getStatusCode().value()).isEqualTo(404);
        assertThat(call(POST, path + "/emergency-message", bearer(other), message).getStatusCode().value()).isEqualTo(404);
        assertThat(call(PUT, path, bearer(owner), updated).getStatusCode().value()).isEqualTo(200);
        assertThat(call(POST, path + "/emergency-message", bearer(owner), message).getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void G03_이름이나_휴대전화번호가_유효하지_않으면_저장하지_않는다() {
        var owner = user(Role.USER);
        assertThat(call(POST, "/guardians", bearer(owner), contact("1234")).getStatusCode().value()).isEqualTo(400);
        assertThat(call(POST, "/guardians", bearer(owner), Map.of(
            "name", " ", "relationship", "가족", "phoneNumber", "01012345678")).getStatusCode().value()).isEqualTo(400);
        assertThat(call(GET, "/guardians", bearer(owner), null).getBody()).isEmpty();
    }

    private Map<String, String> contact(String phone) {
        return Map.of("name", "보호자", "relationship", "가족", "phoneNumber", phone);
    }
}
