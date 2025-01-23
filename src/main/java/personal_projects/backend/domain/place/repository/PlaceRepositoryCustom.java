package personal_projects.backend.domain.place.repository;

import org.springframework.stereotype.Repository;
import personal_projects.backend.domain.place.dto.Search_Type;
import personal_projects.backend.domain.place.dto.response.SearchBookMarkPlaceResponse;
import personal_projects.backend.domain.place.dto.response.SearchDetailPlaceResponse;
import personal_projects.backend.domain.place.dto.response.SearchResultPlaceResponse;

import java.util.List;

@Repository
public interface PlaceRepositoryCustom {
    List<SearchResultPlaceResponse> findPlacesWithinBuffer(double x, double y, double bufferDistance, Search_Type searchType);

    SearchDetailPlaceResponse findPlaceDetailByPlaceId(Long placeId, Long userId);

    List<SearchBookMarkPlaceResponse> findBookMarkPlacesByUserId(Long userId);
}
