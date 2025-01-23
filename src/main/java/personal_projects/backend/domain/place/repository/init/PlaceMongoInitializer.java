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
        mongoTemplate.getCollection("place").deleteMany(new Document());
        placeRepository.findAll().forEach(place -> { // Place 데이터를 MongoPlace로 변환하여 저장

            MongoPlace mongoPlace = MongoPlace.builder()
                .placeId(place.getId())
                .name(place.getName())
                .placeType(place.getPlace_type().name())
                .address(place.getAddress())
                .tel(place.getTel())
                .coordinate(new GeoJsonPoint(place.getCoordinate().getX(), place.getCoordinate().getY()))
                .build();

            mongoTemplate.save(mongoPlace);
        });
    }
}
