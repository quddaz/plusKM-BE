package personal_projects.backend.domain.place.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import personal_projects.backend.domain.place.domain.Place;
import personal_projects.backend.domain.place.dto.request.SearchPlaceRequest;
import personal_projects.backend.domain.place.dto.response.SearchResultPlaceResponse;
import personal_projects.backend.domain.place.repository.PlaceRepository;
import org.locationtech.jts.geom.GeometryFactory;

import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final GeometryFactory geometryFactory;

    public Place findById(Long id) {
        return placeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 id의 place가 없습니다."));
    }

    public List<SearchResultPlaceResponse> getPlacesWithinBuffer(SearchPlaceRequest searchPlaceRequest) {
        Point center = geometryFactory.createPoint(new org.locationtech.jts.geom.Coordinate(searchPlaceRequest.latitude(), searchPlaceRequest.longitude()));
        return placeRepository.findPlacesWithinBuffer(center, searchPlaceRequest.bufferDistance(), searchPlaceRequest.searchType());
    }

}
