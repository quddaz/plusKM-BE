package personal_projects.backend.domain.place.repository;

import personal_projects.backend.domain.place.domain.Place;
import personal_projects.backend.domain.place.dto.Search_Type;
import personal_projects.backend.domain.place.dto.response.SearchBookMarkPlaceResponse;
import personal_projects.backend.domain.place.dto.response.SearchDetailPlaceResponse;
import personal_projects.backend.domain.place.dto.response.SearchResultPlaceResponse;

import java.util.List;
import java.util.Optional;

public interface PlaceRepository {

    Optional<Place> findById(Long id);

    List<Place> findAllByUniqueKeyIn(List<String> keys);

    void deactivateAll();

    void deleteInactivePlaces();

    void batchUpsert(List<Place> places);

    List<SearchResultPlaceResponse> findPlacesWithinBuffer(
        double longitude,
        double latitude,
        double bufferDistance,
        Search_Type searchType
    );

    SearchDetailPlaceResponse findPlaceDetailByPlaceId(Long placeId, Long userId);

    List<SearchBookMarkPlaceResponse> findBookMarkPlacesByUserId(Long userId);
}
