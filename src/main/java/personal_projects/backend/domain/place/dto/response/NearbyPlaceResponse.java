package personal_projects.backend.domain.place.dto.response;

import personal_projects.backend.domain.place.entity.PlaceCategory;

public record NearbyPlaceResponse(
    Long id,
    String name,
    PlaceCategory category,
    String facilityType,
    String address,
    String phoneNumber,
    double longitude,
    double latitude,
    double distanceMeters
) {
}
