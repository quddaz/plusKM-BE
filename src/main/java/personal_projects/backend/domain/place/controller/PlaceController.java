package personal_projects.backend.domain.place.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import personal_projects.backend.domain.auth.dto.AuthenticatedUserPrincipal;
import personal_projects.backend.domain.place.dto.request.NearbyPlaceSearchRequest;
import personal_projects.backend.domain.place.dto.response.BookmarkedPlacesResponse;
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
        @PathVariable(name = "id") long id,
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal
    ) {
        return placeService.findPlaceDetail(id, principal.getUserId());
    }

    @GetMapping("/bookmark")
    @ResponseStatus(HttpStatus.OK)
    public BookmarkedPlacesResponse getBookmarkedPlaces(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return BookmarkedPlacesResponse.from(
            placeService.findBookmarkedPlacesByUserId(principal.getUserId())
        );
    }
}
