package personal_projects.backend.domain.guardian.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import personal_projects.backend.domain.guardian.dto.request.EmergencyMessageRequest;
import personal_projects.backend.domain.guardian.dto.request.GuardianContactRequest;
import personal_projects.backend.domain.guardian.dto.request.GuardianContactUpdateRequest;
import personal_projects.backend.domain.user.type.Role;
import personal_projects.backend.support.MySqlIntegrationTest;

@Tag("integration")
class GuardianIntegrationTest extends MySqlIntegrationTest {

    @Autowired GuardianContactService service;

    @Test
    void 전화번호는_암호화해_저장하고_조회에는_마스킹하며_알림은_SMS_링크로_만든다() {
        var owner = user(Role.USER);
        emergency("A0001", true, 127.1, 37.4);
        var request = new GuardianContactRequest(" 보호자 ", " 가족 ", "010-1234-5678");
        var first = service.create(owner.getId(), request);
        var second = service.create(owner.getId(), request);

        var ciphertexts = jdbc.queryForList("SELECT encrypted_phone_number FROM guardian_contact ORDER BY id", String.class);
        assertThat(ciphertexts).hasSize(2).doesNotHaveDuplicates();
        assertThat(ciphertexts).allSatisfy(value -> assertThat(value).doesNotContain("01012345678"));
        assertThat(service.findAll(owner.getId())).allSatisfy(contact -> {
            assertThat(contact.name()).isEqualTo("보호자");
            assertThat(contact.maskedPhoneNumber()).isEqualTo("010-****-5678");
        });
        var message = service.createEmergencyMessage(owner.getId(), first.id(),
            new EmergencyMessageRequest("A0001", 127.1, 37.4));
        assertThat(message.smsUri()).startsWith("sms:01012345678?body=");
        assertThat(message.message()).contains("테스트 사용자", "보호자", "A0001 응급실", "37.400000,127.100000");
        assertThat(first.id()).isNotEqualTo(second.id());
    }

    @Test
    void 다른_사용자는_수정과_삭제와_알림_생성을_할_수_없다() {
        var owner = user(Role.USER);
        var other = user(Role.USER);
        var guardian = service.create(owner.getId(), new GuardianContactRequest("보호자", "가족", "01012345678"));

        assertThat(service.findAll(other.getId())).isEmpty();
        assertThatThrownBy(() -> service.update(other.getId(), guardian.id(),
            new GuardianContactUpdateRequest("변경", "가족", "01011112222", true)))
            .isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(() -> service.delete(other.getId(), guardian.id())).isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(() -> service.createEmergencyMessage(other.getId(), guardian.id(),
            new EmergencyMessageRequest("A0001", 127.1, 37.4))).isInstanceOf(NoSuchElementException.class);
        assertThat(service.findAll(owner.getId())).hasSize(1);
    }

    @Test
    void 수정은_실제_DB에_반영되고_비활성_보호자에게는_알림을_만들지_않는다() {
        var owner = user(Role.USER);
        var guardian = service.create(owner.getId(), new GuardianContactRequest("보호자", "가족", "01012345678"));
        service.update(owner.getId(), guardian.id(), new GuardianContactUpdateRequest("새이름", "친구", "01011112222", false));

        assertThat(service.findAll(owner.getId())).singleElement().satisfies(contact -> {
            assertThat(contact.name()).isEqualTo("새이름");
            assertThat(contact.maskedPhoneNumber()).isEqualTo("010-****-2222");
            assertThat(contact.active()).isFalse();
        });
        assertThatThrownBy(() -> service.createEmergencyMessage(owner.getId(), guardian.id(),
            new EmergencyMessageRequest("A0001", 127.1, 37.4))).isInstanceOf(IllegalArgumentException.class);
        service.delete(owner.getId(), guardian.id());
        assertThat(service.findAll(owner.getId())).isEmpty();
    }
}
