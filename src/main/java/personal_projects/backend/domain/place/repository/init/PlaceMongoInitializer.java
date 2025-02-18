package personal_projects.backend.domain.place.repository.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Component;
import personal_projects.backend.domain.place.domain.mongo.MongoPlace;
import personal_projects.backend.domain.place.repository.PlaceRepository;
import personal_projects.backend.global.util.DummyDataInit;
import org.bson.Document;

import java.util.List;

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
        // 기존 데이터 삭제
        mongoTemplate.getCollection("place").deleteMany(new Document());

        // MongoPlace 리스트 생성 후 벌크 삽입
        List<MongoPlace> mongoPlaces = placeRepository.findAll()
            .stream()
            .map(place -> MongoPlace.builder()
                .placeId(place.getId())
                .name(place.getName())
                .placeType(place.getPlace_type().name())
                .address(place.getAddress())
                .tel(place.getTel())
                .coordinate(new GeoJsonPoint(place.getCoordinate().getX(), place.getCoordinate().getY()))
                .build())
            .toList();

        // 벌크 저장
        if (!mongoPlaces.isEmpty()) {
            mongoTemplate.insertAll(mongoPlaces);
        }
    }
}
