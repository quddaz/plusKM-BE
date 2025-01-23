package personal_projects.backend.domain.place.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import personal_projects.backend.domain.place.domain.Place;
import personal_projects.backend.domain.place.dto.request.SearchPlaceRequest;
import personal_projects.backend.domain.place.dto.response.SearchDetailPlaceResponse;
import personal_projects.backend.domain.place.dto.response.SearchResultPlaceResponse;
import personal_projects.backend.domain.place.exception.PlaceNotFoundException;
import personal_projects.backend.domain.place.exception.errorCode.PlaceErrorCode;
import personal_projects.backend.domain.place.repository.PlaceRepository;

import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class PlaceService {

    private final PlaceRepository placeRepository;

    public Place findById(Long id) {
        return placeRepository.findById(id).orElseThrow(() -> new PlaceNotFoundException(PlaceErrorCode.PLACE_NOT_FOUND));
    }

    public List<SearchResultPlaceResponse> getPlacesWithinBuffer(SearchPlaceRequest searchPlaceRequest) {
        return placeRepository.findPlacesWithinBuffer(searchPlaceRequest.longitude(), searchPlaceRequest.latitude(),
            searchPlaceRequest.bufferDistance(), searchPlaceRequest.searchType());
    }

    public List<SearchDetailPlaceResponse> findPlaceDetailByPlaceId(Long placeId, Long userId) {
        return placeRepository.findPlaceDetailByPlaceId(placeId, userId);
    }
}
