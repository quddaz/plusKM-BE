package personal_projects.backend.domain.place.repository.init;

import com.opencsv.CSVReader;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import personal_projects.backend.domain.place.domain.Place;
import personal_projects.backend.domain.place.domain.enumType.Place_type;
import personal_projects.backend.domain.place.repository.PlaceRepository;
import personal_projects.backend.domain.place.repository.bulk.PlaceBulkRepository;
import personal_projects.backend.global.util.DummyDataInit;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Order(1)
@DummyDataInit
public class PlaceInitializer implements ApplicationRunner {
    private final PlaceRepository placeRepository;
    private final GeometryFactory geometryFactory;
    private final PlaceBulkRepository placeBulkRepository;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (placeRepository.count() > 0) {
            log.info("[Place] 기존 데이터 갱신 시작");
        } else {
            log.info("[Place] 더미 데이터 삽입 시작");
        }
        importPlace();
    }

    private void importPlace() {
        Map<String, Place> csvDataMap = new HashMap<>();
        syncPlaceData("data/병원정보.csv", 1, 28, 29, 3, 10, 11, csvDataMap);
        syncPlaceData("data/약국정보.csv", 1, 13, 14, 3, 10, 11, csvDataMap);
        updateDatabase(csvDataMap);
    }

    private void syncPlaceData(String filePath, int nameIdx, int longitudeIdx, int latitudeIdx, int placeTypeIdx,
                               int addressIdx, int telIdx, Map<String, Place> csvDataMap) {
        try (
             InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath);
             InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             CSVReader csvReader = new CSVReader(reader)) {

            csvReader.readNext(); // 첫 번째 줄 헤더 건너뜀

            String[] nextLine;
            while ((nextLine = csvReader.readNext()) != null) {
                String name = nextLine[nameIdx];
                String placeTypeStr = nextLine[placeTypeIdx];
                String address = nextLine[addressIdx];
                String tel = nextLine[telIdx];

                Double longitude = null;
                Double latitude = null;

                try {
                    longitude = Double.parseDouble(nextLine[longitudeIdx]);
                    latitude = Double.parseDouble(nextLine[latitudeIdx]);
                } catch (NumberFormatException e) {
                    continue; // 좌표가 없으면 건너뜀
                }

                String uniqueKey = generateUniqueKey(name, address); // 유니크 키 생성
                if (csvDataMap.containsKey(uniqueKey)) {
                    log.warn("중복 데이터 발견 - 유니크 키: {}", uniqueKey);
                    continue;
                }
                Place place = Place.builder()
                    .name(name)
                    .place_type(Place_type.valueOf(placeTypeStr))
                    .address(address)
                    .tel(tel)
                    .coordinate(createPoint(latitude, longitude))
                    .build();

                csvDataMap.put(uniqueKey, place);
            }

        } catch (Exception e) {
            log.error("CSV 파일을 처리하는 중 오류 발생", e);
            throw new RuntimeException("CSV 파일을 처리하는 중 오류 발생", e);
        }
    }
    @Transactional
    protected void updateDatabase(Map<String, Place> csvDataMap) {
        long start = System.currentTimeMillis();

        List<Place> existingPlaces = placeRepository.findAll();

        Map<String, Place> existingPlacesMap = existingPlaces.stream()
            .collect(Collectors.toMap(
                place -> generateUniqueKey(place.getName(), place.getAddress()),
                place -> place,
                (existing, duplicate) -> duplicate
            ));

        List<Place> placesToSave = csvDataMap.entrySet().stream()
            .filter(entry -> !existingPlacesMap.containsKey(entry.getKey()) || isUpdated(entry.getKey(), entry.getValue(), existingPlacesMap))
            .map(Map.Entry::getValue)
            .collect(Collectors.toList());

        List<Long> placeIdsToDelete = existingPlaces.stream()
            .filter(place -> !csvDataMap.containsKey(generateUniqueKey(place.getName(), place.getAddress())))
            .map(Place::getId)
            .collect(Collectors.toList());

        // ✅ 1000개씩 나눠서 처리
        int BATCH_SIZE = 1000;
        for (List<Place> batch : partitionList(placesToSave, BATCH_SIZE)) {
            placeBulkRepository.batchInsertPlaces(batch);
        }

        for (List<Long> batch : partitionList(placeIdsToDelete, BATCH_SIZE)) {
            placeBulkRepository.batchDeletePlaces(batch);
        }

        long end = System.currentTimeMillis();
        log.info("[Place] 데이터 동기화 완료 - 추가/수정: {}, 삭제: {}, 시간: {}ms",
            placesToSave.size(), placeIdsToDelete.size(), end - start);
    }
    private <T> List<List<T>> partitionList(List<T> list, int batchSize) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            partitions.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }
        return partitions;
    }

    private boolean isUpdated(String key, Place newPlace, Map<String, Place> existingPlacesMap) {
        Place existingPlace = existingPlacesMap.get(key);

        if (existingPlace == null) {
            return true; // 기존 데이터가 없는 경우 업데이트 필요
        }

        // 필드별 비교
        return !Objects.equals(existingPlace.getName(), newPlace.getName())
            || !Objects.equals(existingPlace.getPlace_type(), newPlace.getPlace_type())
            || !Objects.equals(existingPlace.getAddress(), newPlace.getAddress())
            || !Objects.equals(existingPlace.getTel(), newPlace.getTel())
            || !existingPlace.getCoordinate().equalsExact(newPlace.getCoordinate());
    }

    private String generateUniqueKey(String name, String address) {
        return (name.trim() + "_" + address.trim()).replaceAll("\\s+", "_");
    }

    private Point createPoint(double latitude, double longitude) {
        Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        point.setSRID(4326);
        return point;
    }
}
