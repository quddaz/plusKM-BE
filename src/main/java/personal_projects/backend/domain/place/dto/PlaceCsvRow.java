package personal_projects.backend.domain.place.dto;

import personal_projects.backend.domain.place.entity.PlaceCategory;

public record PlaceCsvRow(
    String name,
    PlaceCategory category,
    String facilityType,
    String address,
    String phoneNumber,
    double longitude,
    double latitude
) {
}
