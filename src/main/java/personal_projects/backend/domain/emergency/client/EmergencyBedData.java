package personal_projects.backend.domain.emergency.client;

public record EmergencyBedData(
    String hpid,
    Integer emergencyRoom,
    Integer operatingRoom,
    Integer intensiveCareUnit,
    Integer inpatientRoom,
    String updatedAt
) {
}
