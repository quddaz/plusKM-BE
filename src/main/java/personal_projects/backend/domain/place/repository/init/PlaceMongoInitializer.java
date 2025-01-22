package personal_projects.backend.domain.place.repository.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeospatialIndex;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import com.mongodb.client.model.Filters;
import com.opencsv.CSVReader;
import personal_projects.backend.domain.place.domain.mongo.MongoPlace;
import personal_projects.backend.domain.place.repository.PlaceRepository;
import personal_projects.backend.global.util.DummyDataInit;


@Slf4j
@RequiredArgsConstructor
@Component
@Order(2)
@DummyDataInit
public class PlaceMongoInitializer implements ApplicationRunner {

    private final MongoTemplate mongoTemplate;
    private final PlaceRepository placeRepository;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        mongoTemplate.dropCollection(MongoPlace.class); // 기존 데이터 삭제
        mongoTemplate.indexOps(MongoPlace.class).ensureIndex(new GeospatialIndex("coordinate")); // 좌표에 대한 인덱스 생성
        placeRepository.findAll().forEach(place -> { // Place 데이터를 MongoPlace로 변환하여 저장
            GeoJsonPoint geoJsonPoint = new GeoJsonPoint(place.getCoordinate().getX(), place.getCoordinate().getY());

            MongoPlace mongoPlace = MongoPlace.builder()
                .placeId(place.getId())
                .name(place.getName())
                .placeType(place.getPlace_type().name())
                .address(place.getAddress())
                .tel(place.getTel())
                .coordinate(geoJsonPoint)
                .build();

            mongoTemplate.save(mongoPlace);
        });
    }
}
