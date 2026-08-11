package personal_projects.backend.domain.place.dto.request;

import lombok.Builder;
import personal_projects.backend.domain.place.type.PlaceSearchType;

@Builder
public record NearbyPlaceSearchRequest(
    double longitude,
    double latitude,
    double radiusKilometers,
    PlaceSearchType searchType
) {
}
