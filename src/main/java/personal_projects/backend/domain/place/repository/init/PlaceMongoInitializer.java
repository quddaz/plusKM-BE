package personal_projects.backend.domain.place.repository.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
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
        mongoTemplate.dropCollection(MongoPlace.class);
        placeRepository.findAll().forEach(place -> {
            MongoPlace mongoPlace = MongoPlace.builder()
                    .id(place.getId().toString())
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
