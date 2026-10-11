package personal_projects.backend.domain.emergency.importer;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import personal_projects.backend.domain.emergency.client.EmergencyApiClient;
import personal_projects.backend.domain.emergency.client.EmergencyFacilityData;
import personal_projects.backend.domain.emergency.entity.Emergency;
import personal_projects.backend.domain.emergency.repository.EmergencyRepository;

@Component
@RequiredArgsConstructor
public class EmergencyImporter {

    private final EmergencyApiClient apiClient;
    private final EmergencyRepository repository;
    private final GeometryFactory geometryFactory;

    @Transactional
    public void importAll() {
        List<EmergencyFacilityData> facilities = apiClient.fetchFacilities();
        List<Emergency> existingEmergencies = repository.findAllActive();

        deactivateMissingEmergencies(existingEmergencies, extractHpids(facilities));
        List<Emergency> importedEmergencies = mapEmergencies(facilities, existingEmergencies);

        repository.saveAll(existingEmergencies);
        repository.saveAll(importedEmergencies);
    }

    private Set<String> extractHpids(List<EmergencyFacilityData> facilities) {
        return facilities.stream()
            .map(EmergencyFacilityData::hpid)
            .collect(Collectors.toSet());
    }

    private void deactivateMissingEmergencies(List<Emergency> emergencies, Set<String> importedHpids) {
        emergencies.stream()
            .filter(emergency -> !importedHpids.contains(emergency.getHpid()))
            .forEach(Emergency::deactivate);
    }

    private List<Emergency> mapEmergencies(
        List<EmergencyFacilityData> facilities,
        List<Emergency> existingEmergencies
    ) {
        Map<String, Emergency> existingByHpid = indexByHpid(existingEmergencies);
        return facilities.stream()
            .map(facility -> mapEmergency(facility, existingByHpid.get(facility.hpid())))
            .toList();
    }

    private Map<String, Emergency> indexByHpid(List<Emergency> emergencies) {
        return emergencies.stream()
            .filter(emergency -> emergency.getHpid() != null)
            .collect(Collectors.toMap(Emergency::getHpid, Function.identity()));
    }

    private Emergency mapEmergency(EmergencyFacilityData facility, Emergency existing) {
        Point coordinate = createCoordinate(facility);
        Emergency emergency = findOrCreate(facility, existing, coordinate);
        emergency.update(facility.name(), facility.address(), facility.phoneNumber(), coordinate);
        return emergency;
    }

    private Emergency findOrCreate(
        EmergencyFacilityData facility,
        Emergency existing,
        Point coordinate
    ) {
        if (existing != null) {
            return existing;
        }
        return Emergency.create(
            facility.hpid(),
            facility.name(),
            facility.address(),
            facility.phoneNumber(),
            coordinate
        );
    }

    private Point createCoordinate(EmergencyFacilityData facility) {
        Coordinate coordinate = new Coordinate(facility.longitude(), facility.latitude());
        Point point = geometryFactory.createPoint(coordinate);
        point.setSRID(4326);
        return point;
    }
}
