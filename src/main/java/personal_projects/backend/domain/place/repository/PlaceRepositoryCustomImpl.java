package personal_projects.backend.domain.place.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Repository;
import personal_projects.backend.domain.place.domain.Place;
import personal_projects.backend.domain.place.domain.Place_type;
import personal_projects.backend.domain.place.dto.Search_Type;
import personal_projects.backend.domain.place.dto.response.SearchResultPlaceResponse;
import java.util.List;

import static personal_projects.backend.domain.place.domain.QPlace.place;

@Repository
@RequiredArgsConstructor
public class PlaceRepositoryCustomImpl implements PlaceRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<SearchResultPlaceResponse> findPlacesWithinBuffer(Point center, double bufferDistance, Search_Type searchType) {
        BooleanBuilder builder = createSearchConditions(new BooleanBuilder(), center, searchType, bufferDistance);

        return queryFactory.select(Projections.constructor(SearchResultPlaceResponse.class,
                place.id,
                place.name,
                place.address,
                place.tel,
                Expressions.numberTemplate(Double.class, "ST_X({0})", place.coordinate).as("x"), // X 좌표
                Expressions.numberTemplate(Double.class, "ST_Y({0})", place.coordinate).as("y")  // Y 좌표
            ))
            .from(place)
            .where(builder)
            .fetch();
    }

    private BooleanBuilder createSearchConditions(BooleanBuilder builder, Point center, Search_Type searchType, double bufferDistance) {
        // 위도와 경도의 변환 계수
        double lat = center.getY();
        double lon = center.getX();

        // 킬로미터를 위도/경도 차이로 변환
        double latDistance = bufferDistance / 111.32; // 위도 거리 변환 (1도당 111.32 km)
        double lonDistance = bufferDistance / (111.32 * Math.cos(Math.toRadians(lat))); // 경도 거리 변환

        // 계산된 거리로 좌표 범위 설정
        double latMin = lat - latDistance;
        double latMax = lat + latDistance;
        double lonMin = lon - lonDistance;
        double lonMax = lon + lonDistance;

        // 폴리곤 WKT 생성 (위도, 경도 순서로)
        String polygonWKT = String.format("POLYGON((%f %f, %f %f, %f %f, %f %f, %f %f))",
            lonMin, latMin, lonMin, latMax, lonMax, latMax, lonMax, latMin, lonMin, latMin);

        builder.and(Expressions.booleanTemplate(
            "ST_Within({0}, ST_GeomFromText({1}, 4326))", place.coordinate, polygonWKT));

        // SearchType에 따른 추가 조건
        if (searchType == Search_Type.HOSPITAL) {
            builder.and(place.place_type.ne(Place_type.약국)); // 약국이 아닌 병원
        } else if (searchType == Search_Type.PHARMACY) {
            builder.and(place.place_type.eq(Place_type.약국)); // 약국
        }

        return builder;
    }


}
