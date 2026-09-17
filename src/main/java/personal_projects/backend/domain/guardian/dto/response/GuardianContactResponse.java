package personal_projects.backend.domain.guardian.dto.response;

import personal_projects.backend.domain.guardian.entity.GuardianContact;

public record GuardianContactResponse(
    Long id,
    String name,
    String relationship,
    String maskedPhoneNumber,
    boolean active
) {
    public static GuardianContactResponse from(GuardianContact guardianContact, String phoneNumber) {
        return new GuardianContactResponse(
            guardianContact.getId(),
            guardianContact.getName(),
            guardianContact.getRelationship(),
            mask(phoneNumber),
            guardianContact.isActive()
        );
    }

    public static String mask(String phoneNumber) {
        String digits = phoneNumber.replaceAll("[^0-9]", "");
        if (digits.length() < 7) {
            return "****";
        }
        return digits.substring(0, 3) + "-****-" + digits.substring(digits.length() - 4);
    }
}
