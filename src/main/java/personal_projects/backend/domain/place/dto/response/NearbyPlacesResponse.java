package personal_projects.backend.domain.place.dto.response;

import java.util.List;

public record NearbyPlacesResponse(List<NearbyPlaceResponse> places) {
}
