package personal_projects.backend.domain.place.repository.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import personal_projects.backend.domain.place.domain.Place;
import personal_projects.backend.domain.place.dto.Search_Type;
import personal_projects.backend.domain.place.dto.response.SearchBookMarkPlaceResponse;
import personal_projects.backend.domain.place.dto.response.SearchDetailPlaceResponse;
import personal_projects.backend.domain.place.dto.response.SearchResultPlaceResponse;
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
    public List<SearchResultPlaceResponse> findPlacesWithinBuffer(
        double longitude,
        double latitude,
        double bufferDistance,
        Search_Type searchType
    ) {
        return jdbcRepository.findPlacesWithinBuffer(
            longitude,
            latitude,
            bufferDistance,
            searchType
        );
    }

    @Override
    public SearchDetailPlaceResponse findPlaceDetailByPlaceId(Long placeId, Long userId) {
        return jdbcRepository.findPlaceDetailByPlaceId(placeId, userId);
    }

    @Override
    public List<SearchBookMarkPlaceResponse> findBookMarkPlacesByUserId(Long userId) {
        return jdbcRepository.findBookMarkPlacesByUserId(userId);
    }
}
