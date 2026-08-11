package personal_projects.backend.domain.place.importer.service;

import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import personal_projects.backend.domain.place.entity.Place;
import personal_projects.backend.domain.place.type.PlaceType;
import personal_projects.backend.domain.place.repository.PlaceRepository;
import personal_projects.backend.domain.place.importer.PlaceImportProperties;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class PlaceCsvImporter {

    private final PlaceRepository placeRepository;
    private final GeometryFactory geometryFactory;
    private final PlaceImportProperties importProperties;


    public void importPlaces() {

        // 1. 기존 모든 Place 데이터를 비활성화합니다.
        placeRepository.deactivateAll();

        // 2. CSV 데이터를 스트리밍 방식으로 읽어와 DB에 삽입/업데이트합니다.
        readAndPersistCsvData();

        // 3. `readAndPersistCsvData()`을 통해 갱신되지 않아 여전히 `active=false` 상태인
        //    모든 Place 데이터를 삭제합니다.
        placeRepository.deleteInactivePlaces();

        log.info("[Place] 공공데이터 삽입/갱신 완료");
    }

    private void readAndPersistCsvData() {
        Map<String, Place> placesByUniqueKey = new HashMap<>();

        // 병원 및 약국 CSV 파일 데이터를 동기화합니다.
        readCsvFile(importProperties.hospitalPath(), 1, 28, 29, 3, 10, 11, placesByUniqueKey);
        readCsvFile(importProperties.pharmacyPath(), 1, 13, 14, 3, 10, 11, placesByUniqueKey);

        if (!placesByUniqueKey.isEmpty()) {
            upsertBatch(placesByUniqueKey);
        }
    }

    private void readCsvFile(String filePath, int nameIndex, int longitudeIndex, int latitudeIndex, int placeTypeIndex,
                             int addressIndex, int phoneNumberIndex, Map<String, Place> placesByUniqueKey) {
        try (
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath);
            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            CSVReader csvReader = new CSVReader(reader)) {

            csvReader.readNext(); // 헤더를 건너뜁니다.

            String[] row;
            while ((row = csvReader.readNext()) != null) {
                String name = row[nameIndex];
                String placeTypeName = row[placeTypeIndex];
                String address = row[addressIndex];
                String phoneNumber = row[phoneNumberIndex];

                Double longitude;
                Double latitude;

                try {
                    longitude = Double.parseDouble(row[longitudeIndex]);
                    latitude = Double.parseDouble(row[latitudeIndex]);
                } catch (NumberFormatException e) {
                    continue;
                }

                String uniqueKey = createUniqueKey(name, address);
                if (placesByUniqueKey.containsKey(uniqueKey)) {
                    continue;
                }

                if (placeTypeName != null && !placeTypeName.isBlank()) {
                    Place place = Place.builder()
                        .name(name)
                        .placeType(PlaceType.valueOf(placeTypeName))
                        .address(address)
                        .phoneNumber(phoneNumber)
                        .active(true)
                        .coordinate(createLocation(latitude, longitude))
                        .build();

                    placesByUniqueKey.put(uniqueKey, place);
                } else {
                    continue;
                }


                // BATCH_SIZE만큼 데이터가 쌓이면 부분적으로 DB에 반영하고 맵을 비웁니다.
                if (placesByUniqueKey.size() >= importProperties.batchSize()) {
                    upsertBatch(placesByUniqueKey);
                    placesByUniqueKey.clear();
                }
            }

        } catch (Exception e) {
            log.error("CSV 파일을 처리하는 중 오류 발생: {}", filePath, e);
            throw new RuntimeException("CSV 파일 처리 중 오류 발생", e);
        } finally {
            placesByUniqueKey.clear();
        }
    }

    protected void upsertBatch(Map<String, Place> placesByUniqueKey) {
        if (placesByUniqueKey.isEmpty()) {
            return;
        }

        List<String> keys = new ArrayList<>(placesByUniqueKey.keySet());
        // 고유 키(이름 + 주소 조합)로 기존 Place 데이터를 조회합니다.
        List<Place> existingPlaces = placeRepository.findAllByUniqueKeyIn(keys);

        Map<String, Place> existingMap = existingPlaces.stream()
            .collect(Collectors.toMap(
                place -> createUniqueKey(place.getName(), place.getAddress()),
                place -> place
            ));

        // 삽입 또는 업데이트가 필요한 데이터를 선별합니다.
        List<Place> changedPlaces = placesByUniqueKey.entrySet().stream()
            .filter(entry -> hasChanged(existingMap.get(entry.getKey()), entry.getValue()))
            .map(Map.Entry::getValue)
            .toList();

        if (!changedPlaces.isEmpty()) {
            placeRepository.batchUpsert(changedPlaces);
        }
    }

    // 기존 Place와 비교하여 업데이트가 필요한지 확인합니다.
    private boolean hasChanged(Place existingPlace, Place importedPlace) {
        if (existingPlace == null) {
            return true;
        }

        return !Objects.equals(existingPlace.getName(), importedPlace.getName())
            || !Objects.equals(existingPlace.getPlaceType(), importedPlace.getPlaceType())
            || !Objects.equals(existingPlace.getAddress(), importedPlace.getAddress())
            || !Objects.equals(existingPlace.getPhoneNumber(), importedPlace.getPhoneNumber())
            || !existingPlace.getCoordinate().equalsExact(importedPlace.getCoordinate());
    }

    // 이름과 주소를 조합하여 고유 키를 생성합니다.
    private String createUniqueKey(String name, String address) {
        return (name.trim() + "_" + address.trim()).replaceAll("\\s+", "_");
    }

    // 위도, 경도 정보를 사용하여 Point 객체를 생성하고 SRID를 설정합니다.
    private Point createLocation(double latitude, double longitude) {
        Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        point.setSRID(4326);
        return point;
    }
}
