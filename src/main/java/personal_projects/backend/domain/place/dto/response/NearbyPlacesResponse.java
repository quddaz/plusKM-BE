package personal_projects.backend.domain.place.dto.response;

import java.util.List;

public record NearbyPlacesResponse(List<NearbyPlaceResponse> places) {

    public NearbyPlacesResponse {
        places = List.copyOf(places);
    }

    public static NearbyPlacesResponse from(List<NearbyPlaceResponse> places) {
        return new NearbyPlacesResponse(places);
    }
}
