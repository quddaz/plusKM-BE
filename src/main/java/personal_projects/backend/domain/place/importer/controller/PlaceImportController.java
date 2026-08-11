package personal_projects.backend.domain.place.importer.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import personal_projects.backend.domain.auth.dto.AuthenticatedUserPrincipal;
import personal_projects.backend.domain.place.importer.facade.PlaceImportFacade;

@Slf4j
@RestController
@RequestMapping("/admin/place-imports")
@RequiredArgsConstructor
public class PlaceImportController {
    private final PlaceImportFacade placeImportFacade;


    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void importPlacesFromCsv(@AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        log.info("관리자 요청: RDB Place 데이터 갱신 작업 시작.");
        placeImportFacade.importPlaces(principal.getRoles().get(0));
    }
}
