package personal_projects.backend.domain.emergency.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import personal_projects.backend.domain.emergency.dto.request.EmergencyBedSearchRequest;
import personal_projects.backend.domain.emergency.dto.request.NearbyEmergencySearchRequest;
import personal_projects.backend.domain.emergency.dto.response.EmergencyBedsResponse;
import personal_projects.backend.domain.emergency.dto.response.EmergencyDetailResponse;
import personal_projects.backend.domain.emergency.dto.response.NearbyEmergenciesResponse;
import personal_projects.backend.domain.emergency.service.EmergencyBedCacheService;
import personal_projects.backend.domain.emergency.service.EmergencyService;

@RestController
@RequestMapping("/emergencies")
@RequiredArgsConstructor
public class EmergencyController {

    private final EmergencyService emergencyService;
    private final EmergencyBedCacheService emergencyBedCacheService;

    @PostMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public NearbyEmergenciesResponse findNearbyEmergencies(
        @RequestBody NearbyEmergencySearchRequest searchEmergencyRequest) {
        return NearbyEmergenciesResponse.from(emergencyService.findNearbyEmergencies(searchEmergencyRequest));
    }

    @PostMapping("/beds")
    @ResponseStatus(HttpStatus.OK)
    public EmergencyBedsResponse findBeds(@Valid @RequestBody EmergencyBedSearchRequest request) {
        return EmergencyBedsResponse.from(emergencyBedCacheService.findBeds(request.hpids()));
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EmergencyDetailResponse getEmergencyDetail(
        @PathVariable(name = "id") long id
    ) {
        return emergencyService.findEmergencyDetail(id);
    }
}
