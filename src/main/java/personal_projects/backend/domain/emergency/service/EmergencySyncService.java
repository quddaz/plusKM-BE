package personal_projects.backend.domain.emergency.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import personal_projects.backend.domain.emergency.client.EmergencyApiClient;
import personal_projects.backend.domain.emergency.client.EmergencyFacilityData;
import personal_projects.backend.domain.emergency.entity.Emergency;
import personal_projects.backend.domain.emergency.repository.EmergencyRepository;

@Service
@RequiredArgsConstructor
public class EmergencySyncService {
    private final EmergencyApiClient apiClient;
    private final EmergencyRepository repository;
    private final GeometryFactory geometryFactory;

    @Scheduled(fixedDelayString = "${emergency.sync-delay:24h}")
    @Transactional
    public void synchronize() {
        List<EmergencyFacilityData> facilities = apiClient.fetchFacilities();
        Set<String> hpids = facilities.stream().map(EmergencyFacilityData::hpid).collect(Collectors.toSet());
        List<Emergency> existing = repository.findAllActive();
        existing.stream().filter(emergency -> !hpids.contains(emergency.getHpid())).forEach(Emergency::deactivate);
        List<Emergency> updated = facilities.stream().map(this::toEmergency).toList();
        repository.saveAll(existing);
        repository.saveAll(updated);
    }

    private Emergency toEmergency(EmergencyFacilityData facility) {
        Point coordinate = geometryFactory.createPoint(new Coordinate(facility.longitude(), facility.latitude()));
        coordinate.setSRID(4326);
        Emergency emergency = repository.findByHpid(facility.hpid()).orElseGet(() -> Emergency.create(
            facility.hpid(), facility.name(), facility.address(), facility.phoneNumber(), coordinate));
        emergency.update(facility.name(), facility.address(), facility.phoneNumber(), coordinate);
        return emergency;
    }
}
