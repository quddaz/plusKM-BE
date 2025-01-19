package personal_projects.backend.domain.place.repository;

import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Repository;
import personal_projects.backend.domain.place.domain.Place;
import personal_projects.backend.domain.place.dto.Search_Type;
import personal_projects.backend.domain.place.dto.response.SearchResultPlaceResponse;

import java.util.List;

@Repository
public interface PlaceRepositoryCustom {
    List<SearchResultPlaceResponse> findPlacesWithinBuffer(Point center, double bufferDistance, Search_Type searchType);
}
