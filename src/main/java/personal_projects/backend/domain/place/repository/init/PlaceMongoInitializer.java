package personal_projects.backend.domain.place.repository.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;
import personal_projects.backend.domain.place.domain.Place;
import personal_projects.backend.domain.place.domain.mongo.MongoPlace;
import personal_projects.backend.domain.place.repository.PlaceRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
@Slf4j
@RequiredArgsConstructor
@Service
public class PlaceMongoInitializer {

    private final MongoTemplate mongoTemplate;
    private final PlaceRepository placeRepository;

    private static final int BATCH_SIZE = 1000;


    public void updatePlaceDataFromCsv() throws Exception {
        log.info("MongoDB 갱신 실행 시작");

        mongoTemplate.getCollection("place").deleteMany(new org.bson.Document());
        log.info("기존 MongoDB 'place' 컬렉션 데이터 삭제 완료.");

        try (Stream<Place> placeStream = placeRepository.streamAll()) {
            List<MongoPlace> currentBatch = new ArrayList<>();

            placeStream.forEach(place -> {
                MongoPlace mongoPlace = MongoPlace.builder()
                    .placeId(place.getId())
                    .name(place.getName())
                    .placeType(place.getPlace_type().name())
                    .address(place.getAddress())
                    .tel(place.getTel())
                    .coordinate(new GeoJsonPoint(
                        place.getCoordinate().getX(), // longitude
                        place.getCoordinate().getY()  // latitude
                    ))
                    .build();

                currentBatch.add(mongoPlace);

                if (currentBatch.size() >= BATCH_SIZE) {
                    mongoTemplate.insertAll(currentBatch);
                    currentBatch.clear();
                }
            });

            if (!currentBatch.isEmpty()) {
                mongoTemplate.insertAll(currentBatch);
            }

            log.info("MongoDB 갱신 실행 완료. Place 데이터 동기화 과정 종료.");

        } catch (Exception e) {
            log.error("MongoDB 초기화 중 오류 발생", e);
            throw e;
        }
    }
}