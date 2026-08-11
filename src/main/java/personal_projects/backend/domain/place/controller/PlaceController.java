package personal_projects.backend.domain.place.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import personal_projects.backend.domain.oauth.dto.CustomOAuth2User;
import personal_projects.backend.domain.place.dto.request.SearchPlaceRequest;
import personal_projects.backend.domain.place.dto.response.BookmarkedPlacesResponse;
import personal_projects.backend.domain.place.dto.response.SearchDetailPlaceResponse;
import personal_projects.backend.domain.place.dto.response.SearchPlacesResponse;
import personal_projects.backend.domain.place.service.PlaceService;

@RestController
@RequestMapping("/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    @PostMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public SearchPlacesResponse getPlacesWithinBuffer(
        @RequestBody SearchPlaceRequest searchPlaceRequest) {
        return SearchPlacesResponse.from(placeService.getPlacesWithinBuffer(searchPlaceRequest));
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public SearchDetailPlaceResponse getPlaceDetail(
        @PathVariable(name = "id") long id,
        @AuthenticationPrincipal CustomOAuth2User customOAuth2User
    ) {
        return placeService.findPlaceDetailByPlaceId(id, customOAuth2User.getUserId());
    }

    @GetMapping("/bookmark")
    @ResponseStatus(HttpStatus.OK)
    public BookmarkedPlacesResponse getBookMarkPlaces(
        @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        return BookmarkedPlacesResponse.from(
            placeService.findBookMarkPlacesByUserId(customOAuth2User.getUserId())
        );
    }
}
