package personal_projects.backend.domain.place.repository.mongo;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import personal_projects.backend.domain.place.domain.Place_type;
import personal_projects.backend.domain.place.domain.mongo.MongoPlace;
import personal_projects.backend.domain.place.dto.Search_Type;
import personal_projects.backend.domain.place.dto.response.SearchResultPlaceResponse;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class PlaceMongoRepositoryCustomImpl implements PlaceMongoRepositoryCustom {
    private final MongoTemplate mongoTemplate;

    @Override
    public List<SearchResultPlaceResponse> findPlacesWithinBuffer(double x, double y, double bufferDistance, Search_Type searchType) {
        Query query = new Query();

        // GeoJsonPoint를 사용하여 쿼리 생성
        GeoJsonPoint point = new GeoJsonPoint(x, y);

        double radius = bufferDistance * 1000;  // km -> m

        // 쿼리 조건 설정
        addSearchCriteria(query, point, radius, searchType);

        // 쿼리 실행
        List<MongoPlace> places = mongoTemplate.find(query, MongoPlace.class);

        // 결과 변환
        return places.stream()
            .map(place -> new SearchResultPlaceResponse(
                place.getPlaceId(),
                place.getName(),
                place.getAddress(),
                place.getTel(),
                place.getCoordinate().getX(),
                place.getCoordinate().getY()
            ))
            .collect(Collectors.toList());
    }

    private void addSearchCriteria(Query query, GeoJsonPoint point, double radius, Search_Type searchType) {
        // 위치 조건 설정
        query.addCriteria(Criteria.where("coordinate")
            .nearSphere(point)  // GeoJSON 포인트 사용
            .maxDistance(radius));  // 반경 거리 설정

        // searchType에 따른 추가 조건 설정
        if (searchType == Search_Type.HOSPITAL) {
            query.addCriteria(Criteria.where("placeType").ne(Place_type.약국)); // 약국이 아닌 병원
        } else if (searchType == Search_Type.PHARMACY) {
            query.addCriteria(Criteria.where("placeType").is(Place_type.약국)); // 약국
        }
    }
}
