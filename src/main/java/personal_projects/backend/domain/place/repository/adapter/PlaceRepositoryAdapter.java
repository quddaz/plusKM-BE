package personal_projects.backend.domain.place.repository.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import personal_projects.backend.domain.place.entity.Place;
import personal_projects.backend.domain.place.type.PlaceSearchType;
import personal_projects.backend.domain.place.dto.response.BookmarkedPlaceResponse;
import personal_projects.backend.domain.place.dto.response.PlaceDetailResponse;
import personal_projects.backend.domain.place.dto.response.NearbyPlaceResponse;
import personal_projects.backend.domain.place.repository.PlaceRepository;
import personal_projects.backend.domain.place.repository.jdbc.PlaceJdbcRepository;
import personal_projects.backend.domain.place.repository.jpa.PlaceJpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PlaceRepositoryAdapter implements PlaceRepository {

    private final PlaceJpaRepository jpaRepository;
    private final PlaceJdbcRepository jdbcRepository;

    @Override
    public Optional<Place> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Place> findAllByUniqueKeyIn(List<String> keys) {
        return jpaRepository.findAllByUniqueKeyIn(keys);
    }

    @Override
    public void deactivateAll() {
        jpaRepository.deactivateAll();
    }

    @Override
    public void deleteInactivePlaces() {
        jpaRepository.deleteInactivePlaces();
    }

    @Override
    public void batchUpsert(List<Place> places) {
        jdbcRepository.batchUpsert(places);
    }

    @Override
    public List<NearbyPlaceResponse> findNearbyPlaces(
        double longitude,
        double latitude,
        double radiusKilometers,
        PlaceSearchType searchType
    ) {
        return jdbcRepository.findNearbyPlaces(
            longitude,
            latitude,
            radiusKilometers,
            searchType
        );
    }

    @Override
    public PlaceDetailResponse findPlaceDetail(Long placeId, Long userId) {
        return jdbcRepository.findPlaceDetail(placeId, userId);
    }

    @Override
    public List<BookmarkedPlaceResponse> findBookmarkedPlacesByUserId(Long userId) {
        return jdbcRepository.findBookmarkedPlacesByUserId(userId);
    }
}
