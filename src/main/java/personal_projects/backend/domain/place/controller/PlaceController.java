package personal_projects.backend.domain.place.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import personal_projects.backend.domain.oauth.dto.CustomOAuth2User;
import personal_projects.backend.domain.place.dto.request.SearchPlaceRequest;
import personal_projects.backend.domain.place.service.PlaceMongoService;
import personal_projects.backend.domain.place.service.PlaceService;
import personal_projects.backend.global.dto.ResponseTemplate;

@RestController
@RequestMapping("/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;
    private final PlaceMongoService placeMongoService;

    /* MySQL을 사용하는 구버전
    @PostMapping("/buffer")
    public ResponseTemplate<?> getPlacesWithinBuffer(
        @RequestBody SearchPlaceRequest searchPlaceRequest) {
        return ResponseTemplate.from(placeService.getPlacesWithinBuffer(searchPlaceRequest));
    }
    */

    @PostMapping("/mongo")
    public ResponseEntity<?> getPlacesWithinBufferMongo(
        @RequestBody SearchPlaceRequest searchPlaceRequest) {
        return ResponseEntity
            .ok(ResponseTemplate.from(placeMongoService.getPlacesWithinBuffer(searchPlaceRequest)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPlaceDetail(@PathVariable Long id,
                                            @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        return ResponseEntity
            .ok(ResponseTemplate.from(placeService.findPlaceDetailByPlaceId(id, customOAuth2User.getUserId())));
    }
}