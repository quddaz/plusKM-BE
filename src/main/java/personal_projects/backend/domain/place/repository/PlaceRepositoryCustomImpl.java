package personal_projects.backend.domain.place.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import personal_projects.backend.domain.oauth.util.jwt.JwtProperties;
import personal_projects.backend.domain.place.domain.Place_type;
import personal_projects.backend.domain.place.dto.Search_Type;
import personal_projects.backend.domain.place.dto.response.SearchBookMarkPlaceResponse;
import personal_projects.backend.domain.place.dto.response.SearchDetailPlaceResponse;
import personal_projects.backend.domain.place.dto.response.SearchResultPlaceResponse;

import java.util.List;

import static personal_projects.backend.domain.place.domain.QPlace.place;
import static personal_projects.backend.domain.bookmark.domain.QBookMark.bookMark;

@RequiredArgsConstructor
public class PlaceRepositoryCustomImpl implements PlaceRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<SearchResultPlaceResponse> findPlacesWithinBuffer(double x, double y, double bufferDistance, Search_Type searchType) {
        BooleanBuilder builder = createSearchConditions(new BooleanBuilder(), x, y, searchType, bufferDistance);

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


    private BooleanBuilder createSearchConditions(BooleanBuilder builder, double x, double y, Search_Type searchType, double bufferDistance) {
        // 킬로미터를 위도/경도 차이로 변환
        double latDistance = bufferDistance / 111.32; // 위도 거리 변환 (1도당 111.32 km)
        double lonDistance = bufferDistance / (111.32 * Math.cos(Math.toRadians(y))); // 경도 거리 변환

        // 계산된 거리로 좌표 범위 설정
        double latMin = y - latDistance;
        double latMax = y + latDistance;
        double lonMin = x - lonDistance;
        double lonMax = x + lonDistance;

        // 폴리곤 WKT 생성 (위도, 경도 순서로)
        String polygonWKT = String.format("POLYGON((%f %f, %f %f, %f %f, %f %f, %f %f))",
            latMin, lonMin, latMin, lonMax, latMax, lonMax, latMax, lonMin, latMin, lonMin);

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

    @Override
    public SearchDetailPlaceResponse findPlaceDetailByPlaceId(Long placeId, Long userId) {
        return queryFactory.select(Projections.constructor(SearchDetailPlaceResponse.class,
                place.id,
                place.name,
                place.address,
                place.tel,
                place.place_type.stringValue(),
                JPAExpressions.selectOne()
                    .from(bookMark)
                    .where(bookMark.place.id.eq(place.id)
                        .and(bookMark.user.id.eq(userId)))
                    .exists()
            ))
            .from(place)
            .where(place.id.eq(placeId))
            .fetchOne();
    }

    @Override
    public List<SearchBookMarkPlaceResponse> findBookMarkPlacesByUserId(Long userId) {
        return queryFactory.select(Projections.constructor(SearchBookMarkPlaceResponse.class,
                place.id,
                place.name,
                place.address,
                place.tel,
                place.place_type.stringValue()
            ))
            .from(place)
            .join(bookMark.place, place)
            .where(bookMark.user.id.eq(userId))
            .fetch();
    }
}
