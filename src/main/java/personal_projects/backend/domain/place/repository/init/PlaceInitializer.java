package personal_projects.backend.domain.place.repository.init;

import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import personal_projects.backend.domain.place.domain.Place;
import personal_projects.backend.domain.place.domain.Place_type;
import personal_projects.backend.domain.place.repository.PlaceRepository;
import personal_projects.backend.global.util.DummyDataInit;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Order(1)
@DummyDataInit
public class PlaceInitializer implements ApplicationRunner {
    private final PlaceRepository placeRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (placeRepository.count() > 0) {
            log.info("[Place] 더미 데이터 존재");
        } else {
            importPlace();
        }
    }

    private void importPlace() {
        // 병원과 약국 데이터를 각기 다른 CSV 파일에서 읽어오기
        importCsvToPlace("data/병원정보.csv", 1, 28, 29, 3, 10, 11);
        importCsvToPlace("data/약국정보.csv", 1, 13, 14, 3, 10, 11);
    }

    private void importCsvToPlace(String filePath, int nameIdx, int longitudeIdx, int latitudeIdx, int placeTypeIdx,
                                  int addressIdx, int telIdx) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath);
             InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {

            List<Place> places = new ArrayList<>();

            CSVReader csvReader = new CSVReader(reader);
            String[] nextLine;

            // 첫 번째 줄은 헤더이므로 건너뜁니다.
            csvReader.readNext();

            while ((nextLine = csvReader.readNext()) != null) {
                String name = nextLine[nameIdx];  // 이름
                String placeTypeStr = nextLine[placeTypeIdx];  // 장소 타입
                String address = nextLine[addressIdx];  // 주소
                String tel = nextLine[telIdx];  // 전화번호

                Double longitude = null;
                Double latitude = null;

                try {
                    // 좌표 (경도, 위도)
                    longitude = Double.parseDouble(nextLine[longitudeIdx]);
                    latitude = Double.parseDouble(nextLine[latitudeIdx]);
                } catch (NumberFormatException e) {
                }

                if (longitude == null || latitude == null) {
                    // 좌표가 없을 경우 건너뛰기
                    continue;  // 좌표가 없으면 해당 항목은 건너뜁니다.
                }
                // Place 객체 생성
                Place place = Place.builder()
                    .name(name)
                    .place_type(Place_type.valueOf(placeTypeStr))  // Place_type ENUM 사용
                    .address(address)
                    .tel(tel)
                    .coordinate(createPoint(latitude, longitude))  // Point 객체 생성
                    .build();

                places.add(place);
            }
            placeRepository.saveAll(places);
        } catch (Exception e) {
            log.error("CSV 파일을 처리하는 중 오류 발생", e);
            throw new RuntimeException("CSV 파일을 처리하는 중 오류 발생", e);
        }
    }

    private Point createPoint(double latitude, double longitude) {
        GeometryFactory geometryFactory = new GeometryFactory();
        Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        point.setSRID(4326);  // SRID 설정
        return point;
    }
}