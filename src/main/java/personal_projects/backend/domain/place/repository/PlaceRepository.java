package personal_projects.backend.domain.place.repository;

import personal_projects.backend.domain.place.entity.Place;
import personal_projects.backend.domain.place.type.PlaceSearchType;
import personal_projects.backend.domain.place.dto.response.BookmarkedPlaceResponse;
import personal_projects.backend.domain.place.dto.response.PlaceDetailResponse;
import personal_projects.backend.domain.place.dto.response.NearbyPlaceResponse;

import java.util.List;
import java.util.Optional;

public interface PlaceRepository {

    Optional<Place> findById(Long id);

    List<Place> findAllByUniqueKeyIn(List<String> keys);

    void deactivateAll();

    void deleteInactivePlaces();

    void batchUpsert(List<Place> places);

    List<NearbyPlaceResponse> findNearbyPlaces(
        double longitude,
        double latitude,
        double radiusKilometers,
        PlaceSearchType searchType
    );

    PlaceDetailResponse findPlaceDetail(Long placeId, Long userId);

    List<BookmarkedPlaceResponse> findBookmarkedPlacesByUserId(Long userId);
}
