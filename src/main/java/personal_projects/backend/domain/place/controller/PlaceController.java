package personal_projects.backend.domain.place.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import personal_projects.backend.domain.place.dto.request.NearbyPlaceSearchRequest;
import personal_projects.backend.domain.place.dto.response.PlaceDetailResponse;
import personal_projects.backend.domain.place.dto.response.NearbyPlacesResponse;
import personal_projects.backend.domain.place.service.PlaceService;

@RestController
@RequestMapping("/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    @PostMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public NearbyPlacesResponse findNearbyPlaces(
        @RequestBody NearbyPlaceSearchRequest searchPlaceRequest) {
        return NearbyPlacesResponse.from(placeService.findNearbyPlaces(searchPlaceRequest));
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PlaceDetailResponse getPlaceDetail(
        @PathVariable(name = "id") long id
    ) {
        return placeService.findPlaceDetail(id);
    }
}
