package personal_projects.backend.domain.emergency.client;

public record EmergencyFacilityData(
    String hpid,
    String name,
    String address,
    String phoneNumber,
    double longitude,
    double latitude
) {
}
