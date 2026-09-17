package personal_projects.backend.domain.guardian.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import personal_projects.backend.domain.emergency.entity.Emergency;
import personal_projects.backend.domain.emergency.service.EmergencyService;
import personal_projects.backend.domain.guardian.dto.request.EmergencyMessageRequest;
import personal_projects.backend.domain.guardian.dto.request.GuardianContactRequest;
import personal_projects.backend.domain.guardian.dto.response.EmergencyMessageResponse;
import personal_projects.backend.domain.guardian.dto.response.GuardianContactResponse;
import personal_projects.backend.domain.guardian.crypto.GuardianPhoneCipher;
import personal_projects.backend.domain.guardian.entity.GuardianContact;
import personal_projects.backend.domain.guardian.repository.GuardianContactRepository;
import personal_projects.backend.domain.user.entity.User;
import personal_projects.backend.domain.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class GuardianContactServiceTest {

    @InjectMocks
    private GuardianContactService service;

    @Mock
    private GuardianContactRepository repository;

    @Mock
    private UserService userService;

    @Mock
    private EmergencyService emergencyService;

    @Mock
    private GuardianPhoneCipher phoneCipher;

    @Test
    void 보호자_이름과_전화번호를_등록한다() {
        User user = User.builder().id(1L).name("홍길동").build();
        when(userService.findById(1L)).thenReturn(user);
        when(phoneCipher.encrypt("01012345678")).thenReturn("encrypted-phone");
        when(repository.save(any(GuardianContact.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        GuardianContactResponse response = service.create(
            1L,
            new GuardianContactRequest("김보호", "부모", "010-1234-5678")
        );

        ArgumentCaptor<GuardianContact> captor = ArgumentCaptor.forClass(GuardianContact.class);
        org.mockito.Mockito.verify(repository).save(captor.capture());
        assertThat(captor.getValue().getEncryptedPhoneNumber()).isEqualTo("encrypted-phone");
        assertThat(response.name()).isEqualTo("김보호");
        assertThat(response.maskedPhoneNumber()).isEqualTo("010-****-5678");
    }

    @Test
    void 긴급문자에_사용자와_보호자_이름과_병원_위치를_포함한다() {
        User user = User.builder().id(1L).name("홍길동").build();
        GuardianContact guardian = GuardianContact.create(user, "김보호", "부모", "encrypted-phone");
        Emergency emergency = Emergency.builder()
            .id(3L)
            .name("서울응급병원")
            .phoneNumber("02-1234-5678")
            .build();
        when(repository.findByIdAndUserId(2L, 1L)).thenReturn(Optional.of(guardian));
        when(emergencyService.findByHpid("A1234567")).thenReturn(emergency);
        when(phoneCipher.decrypt("encrypted-phone")).thenReturn("01012345678");

        EmergencyMessageResponse response = service.createEmergencyMessage(
            1L,
            2L,
            new EmergencyMessageRequest("A1234567", 126.9780, 37.5665)
        );

        assertThat(response.guardianName()).isEqualTo("김보호");
        assertThat(response.maskedPhoneNumber()).isEqualTo("010-****-5678");
        assertThat(response.message())
            .contains("홍길동님의 보호자 김보호님")
            .contains("서울응급병원")
            .contains("37.566500,126.978000")
            .contains("02-1234-5678");
        assertThat(response.smsUri())
            .startsWith("sms:01012345678?body=")
            .contains("%5BplusKM%20%EA%B8%B4%EA%B8%89%20%EC%95%8C%EB%A6%BC%5D");
    }

    @Test
    void 다른_사용자의_보호자는_조회할_수_없다() {
        when(repository.findByIdAndUserId(2L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createEmergencyMessage(
            1L,
            2L,
            new EmergencyMessageRequest("A1234567", 126.9780, 37.5665)
        )).isInstanceOf(NoSuchElementException.class);
    }
}
