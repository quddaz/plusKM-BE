package personal_projects.backend.domain.place.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import personal_projects.backend.domain.place.dto.response.PlaceCsvImportResponse;
import personal_projects.backend.domain.place.service.PlaceCsvImportService;

@RestController
@RequestMapping("/admin/places")
@RequiredArgsConstructor
public class PlaceAdminController {

    private final PlaceCsvImportService importService;

    @PostMapping("/import-csv")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public PlaceCsvImportResponse importCsv() {
        return importService.importAll();
    }
}
