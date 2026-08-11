package personal_projects.backend.domain.place.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import personal_projects.backend.domain.place.entity.Place;
import personal_projects.backend.domain.place.dto.request.NearbyPlaceSearchRequest;
import personal_projects.backend.domain.place.dto.response.BookmarkedPlaceResponse;
import personal_projects.backend.domain.place.dto.response.PlaceDetailResponse;
import personal_projects.backend.domain.place.dto.response.NearbyPlaceResponse;
import personal_projects.backend.common.exception.BusinessException;
import personal_projects.backend.domain.place.exception.code.PlaceErrorCode;
import personal_projects.backend.domain.place.repository.PlaceRepository;

import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class PlaceService {

    private final PlaceRepository placeRepository;

    public Place findById(Long id) {
        return placeRepository.findById(id).orElseThrow(() -> new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND));
    }

    public List<NearbyPlaceResponse> findNearbyPlaces(NearbyPlaceSearchRequest request) {
        return placeRepository.findNearbyPlaces(
            request.longitude(),
            request.latitude(),
            request.radiusKilometers(),
            request.searchType()
        );
    }

    public PlaceDetailResponse findPlaceDetail(Long placeId, Long userId) {
        return placeRepository.findPlaceDetail(placeId, userId);
    }

    public List<BookmarkedPlaceResponse> findBookmarkedPlacesByUserId(Long userId) {
        return placeRepository.findBookmarkedPlacesByUserId(userId);
    }
}
