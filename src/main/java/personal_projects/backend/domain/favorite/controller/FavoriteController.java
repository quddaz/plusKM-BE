package personal_projects.backend.domain.favorite.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import personal_projects.backend.domain.auth.dto.AuthenticatedUserPrincipal;
import personal_projects.backend.domain.favorite.dto.FavoriteResponse;
import personal_projects.backend.domain.favorite.service.FavoriteService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/favorites")
public class FavoriteController {

    private final FavoriteService service;

    @GetMapping
    public List<FavoriteResponse> findAll(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return service.findAll(principal.getUserId());
    }

    @PutMapping("/emergencies/{emergencyId}")
    public FavoriteResponse addEmergency(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable Long emergencyId
    ) {
        return service.addEmergency(principal.getUserId(), emergencyId);
    }

    @DeleteMapping("/emergencies/{emergencyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeEmergency(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable Long emergencyId
    ) {
        service.removeEmergency(principal.getUserId(), emergencyId);
    }

    @PutMapping("/places/{placeId}")
    public FavoriteResponse addPlace(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable Long placeId
    ) {
        return service.addPlace(principal.getUserId(), placeId);
    }

    @DeleteMapping("/places/{placeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removePlace(
        @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
        @PathVariable Long placeId
    ) {
        service.removePlace(principal.getUserId(), placeId);
    }
}
