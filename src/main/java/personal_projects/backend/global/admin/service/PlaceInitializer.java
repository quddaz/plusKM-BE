package personal_projects.backend.global.admin.service;

import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import personal_projects.backend.domain.place.domain.Place;
import personal_projects.backend.domain.place.domain.enumType.Place_type;
import personal_projects.backend.domain.place.repository.PlaceRepository;
import personal_projects.backend.domain.place.repository.bulk.PlaceBulkRepository;
import personal_projects.backend.global.admin.CsvProperties;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class PlaceInitializer{

    private final PlaceRepository placeRepository;
    private final GeometryFactory geometryFactory;
    private final PlaceBulkRepository placeBulkRepository;
    private final CsvProperties csvProperties;


    public void updatePlaceDataFromCsv() {

        // 1. 기존 모든 Place 데이터를 비활성화합니다.
        placeRepository.deactivateAll();

        // 2. CSV 데이터를 스트리밍 방식으로 읽어와 DB에 삽입/업데이트합니다.
        importPlaceStreaming();

        // 3. `importPlaceStreaming()`을 통해 갱신되지 않아 여전히 `active=false` 상태인
        //    모든 Place 데이터를 삭제합니다.
        placeRepository.deleteInactivePlaces();

        log.info("[Place] 공공데이터 삽입/갱신 완료");
    }

    private void importPlaceStreaming() {
        Map<String, Place> csvDataMap = new HashMap<>();

        // 병원 및 약국 CSV 파일 데이터를 동기화합니다.
        syncPlaceData(csvProperties.hospitalPath(), 1, 28, 29, 3, 10, 11, csvDataMap);
        syncPlaceData(csvProperties.pharmacyPath(), 1, 13, 14, 3, 10, 11, csvDataMap);

        if (!csvDataMap.isEmpty()) {
            updateDatabasePartial(csvDataMap);
        }
    }

    private void syncPlaceData(String filePath, int nameIdx, int longitudeIdx, int latitudeIdx, int placeTypeIdx,
                               int addressIdx, int telIdx, Map<String, Place> csvDataMap) {
        try (
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath);
            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            CSVReader csvReader = new CSVReader(reader)) {

            csvReader.readNext(); // 헤더를 건너뜁니다.

            String[] nextLine;
            while ((nextLine = csvReader.readNext()) != null) {
                String name = nextLine[nameIdx];
                String placeTypeStr = nextLine[placeTypeIdx];
                String address = nextLine[addressIdx];
                String tel = nextLine[telIdx];

                Double longitude;
                Double latitude;

                try {
                    longitude = Double.parseDouble(nextLine[longitudeIdx]);
                    latitude = Double.parseDouble(nextLine[latitudeIdx]);
                } catch (NumberFormatException e) {
                    continue;
                }

                String uniqueKey = generateUniqueKey(name, address);
                if (csvDataMap.containsKey(uniqueKey)) {
                    continue;
                }

                if (placeTypeStr != null && !placeTypeStr.trim().isEmpty()) {
                    Place place = Place.builder()
                        .name(name)
                        .place_type(Place_type.valueOf(placeTypeStr))
                        .address(address)
                        .tel(tel)
                        .active(true)
                        .coordinate(createPoint(latitude, longitude))
                        .build();

                    csvDataMap.put(uniqueKey, place);
                } else {
                    continue;
                }


                // BATCH_SIZE만큼 데이터가 쌓이면 부분적으로 DB에 반영하고 맵을 비웁니다.
                if (csvDataMap.size() >= csvProperties.batchSize()) {
                    updateDatabasePartial(csvDataMap);
                    csvDataMap.clear();
                }
            }

        } catch (Exception e) {
            log.error("CSV 파일을 처리하는 중 오류 발생: {}", filePath, e);
            throw new RuntimeException("CSV 파일 처리 중 오류 발생", e);
        } finally {
            csvDataMap.clear();
        }
    }

    protected void updateDatabasePartial(Map<String, Place> csvDataMap) {
        if (csvDataMap.isEmpty()) {
            return;
        }

        List<String> keys = new ArrayList<>(csvDataMap.keySet());
        // 고유 키(이름 + 주소 조합)로 기존 Place 데이터를 조회합니다.
        List<Place> existingPlaces = placeRepository.findAllByUniqueKeyIn(keys);

        Map<String, Place> existingMap = existingPlaces.stream()
            .collect(Collectors.toMap(
                p -> generateUniqueKey(p.getName(), p.getAddress()),
                p -> p
            ));

        // 삽입 또는 업데이트가 필요한 데이터를 선별합니다.
        List<Place> toSave = csvDataMap.entrySet().stream()
            .filter(entry -> !existingMap.containsKey(entry.getKey()) || isUpdated(entry.getKey(), entry.getValue(), existingMap))
            .map(Map.Entry::getValue)
            .collect(Collectors.toList());

        if (!toSave.isEmpty()) {
            placeBulkRepository.batchInsertPlaces(toSave);
        }
    }

    // 기존 Place와 비교하여 업데이트가 필요한지 확인합니다.
    private boolean isUpdated(String key, Place newPlace, Map<String, Place> existingPlacesMap) {
        Place existingPlace = existingPlacesMap.get(key);
        if (existingPlace == null) return true;

        return !Objects.equals(existingPlace.getName(), newPlace.getName())
            || !Objects.equals(existingPlace.getPlace_type(), newPlace.getPlace_type())
            || !Objects.equals(existingPlace.getAddress(), newPlace.getAddress())
            || !Objects.equals(existingPlace.getTel(), newPlace.getTel())
            || !existingPlace.getCoordinate().equalsExact(newPlace.getCoordinate());
    }

    // 이름과 주소를 조합하여 고유 키를 생성합니다.
    private String generateUniqueKey(String name, String address) {
        return (name.trim() + "_" + address.trim()).replaceAll("\\s+", "_");
    }

    // 위도, 경도 정보를 사용하여 Point 객체를 생성하고 SRID를 설정합니다.
    private Point createPoint(double latitude, double longitude) {
        Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        point.setSRID(4326);
        return point;
    }
}