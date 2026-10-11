package personal_projects.backend.domain.place.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import personal_projects.backend.domain.place.dto.request.NearbyPlaceSearchRequest;
import personal_projects.backend.domain.place.dto.response.NearbyPlacesResponse;
import personal_projects.backend.domain.place.service.PlaceService;

@RestController
@RequestMapping("/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    @PostMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public NearbyPlacesResponse findNearby(@Valid @RequestBody NearbyPlaceSearchRequest request) {
        return new NearbyPlacesResponse(placeService.findNearby(request));
    }
}
