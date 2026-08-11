package personal_projects.backend.domain.place.dto.response;

import java.util.List;

public record BookmarkedPlacesResponse(List<SearchBookMarkPlaceResponse> places) {

    public BookmarkedPlacesResponse {
        places = List.copyOf(places);
    }

    public static BookmarkedPlacesResponse from(List<SearchBookMarkPlaceResponse> places) {
        return new BookmarkedPlacesResponse(places);
    }
}
