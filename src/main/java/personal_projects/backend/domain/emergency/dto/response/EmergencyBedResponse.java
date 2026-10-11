package personal_projects.backend.domain.emergency.dto.response;

import personal_projects.backend.domain.emergency.client.EmergencyBedData;

public record EmergencyBedResponse(
    String hpid,
    Integer emergencyRoom,
    Integer operatingRoom,
    Integer intensiveCareUnit,
    Integer inpatientRoom,
    String updatedAt
) {
    public static EmergencyBedResponse from(EmergencyBedData bed) {
        return new EmergencyBedResponse(
            bed.hpid(),
            bed.emergencyRoom(),
            bed.operatingRoom(),
            bed.intensiveCareUnit(),
            bed.inpatientRoom(),
            bed.updatedAt()
        );
    }
}
