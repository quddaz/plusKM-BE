package personal_projects.backend.domain.place.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import personal_projects.backend.domain.place.dto.request.SearchPlaceRequest;
import personal_projects.backend.domain.place.dto.response.SearchResultPlaceResponse;
import personal_projects.backend.domain.place.repository.mongo.PlaceMongoRepository;

import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class PlaceMongoService {
    private final PlaceMongoRepository placeMongoRepository;

    public List<SearchResultPlaceResponse> getPlacesWithinBuffer(SearchPlaceRequest searchPlaceRequest) {
        return placeMongoRepository.findPlacesWithinBuffer(searchPlaceRequest.longitude(), searchPlaceRequest.latitude(),
            searchPlaceRequest.bufferDistance(), searchPlaceRequest.searchType());
    }
}
