package personal_projects.backend.domain.guardian.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import personal_projects.backend.domain.emergency.entity.Emergency;
import personal_projects.backend.domain.emergency.service.EmergencyService;
import personal_projects.backend.domain.guardian.dto.request.EmergencyMessageRequest;
import personal_projects.backend.domain.guardian.dto.request.GuardianContactRequest;
import personal_projects.backend.domain.guardian.dto.request.GuardianContactUpdateRequest;
import personal_projects.backend.domain.guardian.dto.response.EmergencyMessageResponse;
import personal_projects.backend.domain.guardian.dto.response.GuardianContactResponse;
import personal_projects.backend.domain.guardian.crypto.GuardianPhoneCipher;
import personal_projects.backend.domain.guardian.entity.GuardianContact;
import personal_projects.backend.domain.guardian.repository.GuardianContactRepository;
import personal_projects.backend.domain.user.entity.User;
import personal_projects.backend.domain.user.service.UserService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuardianContactService {

    private final GuardianContactRepository repository;
    private final UserService userService;
    private final EmergencyService emergencyService;
    private final GuardianPhoneCipher phoneCipher;

    @Transactional
    public GuardianContactResponse create(Long userId, GuardianContactRequest request) {
        User user = userService.findById(userId);
        String phoneNumber = normalizePhoneNumber(request.phoneNumber());
        GuardianContact guardianContact = GuardianContact.create(
            user,
            request.name().trim(),
            request.relationship().trim(),
            phoneCipher.encrypt(phoneNumber)
        );
        return GuardianContactResponse.from(repository.save(guardianContact), phoneNumber);
    }

    public List<GuardianContactResponse> findAll(Long userId) {
        return repository.findAllByUserId(userId).stream()
            .map(guardian -> GuardianContactResponse.from(
                guardian,
                phoneCipher.decrypt(guardian.getEncryptedPhoneNumber())
            ))
            .toList();
    }

    @Transactional
    public GuardianContactResponse update(
        Long userId,
        Long guardianId,
        GuardianContactUpdateRequest request
    ) {
        GuardianContact guardianContact = findOwnedGuardian(userId, guardianId);
        String phoneNumber = normalizePhoneNumber(request.phoneNumber());
        guardianContact.update(
            request.name().trim(),
            request.relationship().trim(),
            phoneCipher.encrypt(phoneNumber),
            request.active()
        );
        return GuardianContactResponse.from(guardianContact, phoneNumber);
    }

    @Transactional
    public void delete(Long userId, Long guardianId) {
        repository.delete(findOwnedGuardian(userId, guardianId));
    }

    public EmergencyMessageResponse createEmergencyMessage(
        Long userId,
        Long guardianId,
        EmergencyMessageRequest request
    ) {
        GuardianContact guardianContact = findOwnedGuardian(userId, guardianId);
        if (!guardianContact.isActive()) {
            throw new IllegalArgumentException("비활성화된 보호자에게는 알림을 보낼 수 없습니다.");
        }
        Emergency emergency = emergencyService.findByHpid(request.emergencyHpid());
        String phoneNumber = phoneCipher.decrypt(guardianContact.getEncryptedPhoneNumber());
        String message = emergencyMessage(
            guardianContact.getUser().getName(),
            guardianContact.getName(),
            emergency,
            request.longitude(),
            request.latitude()
        );
        return new EmergencyMessageResponse(
            guardianContact.getName(),
            GuardianContactResponse.mask(phoneNumber),
            message,
            smsUri(phoneNumber, message)
        );
    }

    private GuardianContact findOwnedGuardian(Long userId, Long guardianId) {
        return repository.findByIdAndUserId(guardianId, userId)
            .orElseThrow(() -> new NoSuchElementException("보호자 정보를 찾을 수 없습니다."));
    }

    private String emergencyMessage(
        String userName,
        String guardianName,
        Emergency emergency,
        double longitude,
        double latitude
    ) {
        return """
            [plusKM 긴급 알림]
            %s님의 보호자 %s님께 알립니다.
            %s님이 %s으로 이동을 준비하고 있습니다.
            현재 위치: https://maps.google.com/?q=%f,%f
            병원 전화: %s
            긴급한 상황이라면 119 또는 사용자에게 직접 연락해 주세요.
            """.formatted(
                userName,
                guardianName,
                userName,
                emergency.getName(),
                latitude,
                longitude,
                emergency.getPhoneNumber()
            ).trim();
    }

    private String normalizePhoneNumber(String phoneNumber) {
        return phoneNumber.replaceAll("[^0-9]", "");
    }

    private String smsUri(String phoneNumber, String message) {
        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8)
            .replace("+", "%20");
        return "sms:%s?body=%s".formatted(phoneNumber, encodedMessage);
    }
}
