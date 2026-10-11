package personal_projects.backend.domain.place.dto.response;

public record PlaceCsvImportResponse(
    int hospitals,
    int pharmacies,
    int skipped,
    int deletedInactive
) {
}
