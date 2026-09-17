package personal_projects.backend.domain.guardian.dto.response;

public record EmergencyMessageResponse(
    String guardianName,
    String maskedPhoneNumber,
    String message,
    String smsUri
) {
}
