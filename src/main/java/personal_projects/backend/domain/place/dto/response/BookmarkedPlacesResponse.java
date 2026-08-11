package personal_projects.backend.domain.place.dto.response;

import java.util.List;

public record BookmarkedPlacesResponse(List<BookmarkedPlaceResponse> places) {

    public BookmarkedPlacesResponse {
        places = List.copyOf(places);
    }

    public static BookmarkedPlacesResponse from(List<BookmarkedPlaceResponse> places) {
        return new BookmarkedPlacesResponse(places);
    }
}
