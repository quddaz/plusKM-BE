package personal_projects.backend.domain.place.service;

import com.mongodb.MongoException;
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
    private final PlaceService placeService;

    public List<SearchResultPlaceResponse> getPlacesWithinBuffer(SearchPlaceRequest searchPlaceRequest) {
        try{
            return placeMongoRepository.findPlacesWithinBuffer(searchPlaceRequest.longitude(), searchPlaceRequest.latitude(),
                searchPlaceRequest.bufferDistance(), searchPlaceRequest.searchType());
        }catch (MongoException e) {
            log.warn("MongoDB 조회 실패. MySQL fallback 수행", e);
            return placeService.getPlacesWithinBuffer(searchPlaceRequest);
        }
    }
}
