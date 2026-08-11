package personal_projects.backend.domain.place.dto.response;

import java.util.List;

public record SearchPlacesResponse(List<SearchResultPlaceResponse> places) {

    public SearchPlacesResponse {
        places = List.copyOf(places);
    }

    public static SearchPlacesResponse from(List<SearchResultPlaceResponse> places) {
        return new SearchPlacesResponse(places);
    }
}
