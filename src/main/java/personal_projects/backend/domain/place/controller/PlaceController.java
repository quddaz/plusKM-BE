package personal_projects.backend.domain.place.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import personal_projects.backend.domain.place.dto.request.SearchPlaceRequest;
import personal_projects.backend.domain.place.service.PlaceService;
import personal_projects.backend.global.dto.ResponseTemplate;

@RestController
@RequestMapping("/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    @PostMapping("/buffer")
    public ResponseTemplate<?> getPlacesWithinBuffer(
        @RequestBody SearchPlaceRequest searchPlaceRequest) {
        return ResponseTemplate.from(placeService.getPlacesWithinBuffer(searchPlaceRequest));
    }
}