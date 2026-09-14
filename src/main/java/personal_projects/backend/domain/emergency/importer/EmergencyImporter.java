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
        Set<String> hpids = facilities.stream()
            .map(EmergencyFacilityData::hpid)
            .collect(Collectors.toSet());
        List<Emergency> existing = repository.findAllActive();
        existing.stream()
            .filter(emergency -> !hpids.contains(emergency.getHpid()))
            .forEach(Emergency::deactivate);
        Map<String, Emergency> existingByHpid = existing.stream()
            .filter(emergency -> emergency.getHpid() != null)
            .collect(Collectors.toMap(Emergency::getHpid, Function.identity()));
        List<Emergency> updated = facilities.stream()
            .map(facility -> toEmergency(facility, existingByHpid.get(facility.hpid())))
            .toList();

        repository.saveAll(existing);
        repository.saveAll(updated);
    }

    private Emergency toEmergency(EmergencyFacilityData facility, Emergency existing) {
        Point coordinate = geometryFactory.createPoint(
            new Coordinate(facility.longitude(), facility.latitude())
        );
        coordinate.setSRID(4326);
        Emergency emergency = existing == null
            ? Emergency.create(
                facility.hpid(),
                facility.name(),
                facility.address(),
                facility.phoneNumber(),
                coordinate
            )
            : existing;
        emergency.update(facility.name(), facility.address(), facility.phoneNumber(), coordinate);
        return emergency;
    }
}
