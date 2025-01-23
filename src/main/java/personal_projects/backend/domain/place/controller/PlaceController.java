package personal_projects.backend.domain.place.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
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
    @PostMapping("/buffer")
    public ResponseTemplate<?> getPlacesWithinBuffer(
        @RequestBody SearchPlaceRequest searchPlaceRequest) {
        return ResponseTemplate.from(placeService.getPlacesWithinBuffer(searchPlaceRequest));
    }
    @PostMapping("/mongo")
    public ResponseTemplate<?> getPlacesWithinBufferMongo(
        @RequestBody SearchPlaceRequest searchPlaceRequest) {
        return ResponseTemplate.from(placeMongoService.getPlacesWithinBuffer(searchPlaceRequest));
    }
}