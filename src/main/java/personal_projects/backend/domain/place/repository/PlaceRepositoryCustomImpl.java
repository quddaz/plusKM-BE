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
        String pointWKT = String.format("POINT(%f %f)", center.getX(), center.getY());

        // bufferDistance는 km 단위로 받아온다. m 단위로 변환
        double meterRange = bufferDistance * 1000;
        // 경도, 위도에서 0.01도는 1100m인 것을 사용해 몇 m는 위도, 경도로 어느 정도인지 계산
        double meterToDegree = meterRange * 0.01 / 1100;

        builder.and(Expressions.booleanTemplate(
            "ST_Intersects({0}, ST_Buffer(ST_GeomFromText({1}, 4326), {2}))",
            place.coordinate, pointWKT, meterToDegree));

        if (searchType == Search_Type.HOSPITAL) {
            builder.and(place.place_type.ne(Place_type.약국)); // 약국이 아닌 병원
        } else if (searchType == Search_Type.PHARMACY) {
            builder.and(place.place_type.eq(Place_type.약국)); // 약국
        }

        return builder;
    }

}
