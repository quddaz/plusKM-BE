package personal_projects.backend.domain.place.importer;

import personal_projects.backend.domain.place.dto.PlaceCsvRow;
import personal_projects.backend.domain.place.entity.PlaceCategory;

/** 공공데이터 병원·약국 CSV의 컬럼 위치와 행 파싱 규칙. */
public enum PlaceCsvFormat {

    HOSPITAL(PlaceCategory.HOSPITAL, 1, 3, 10, 11, 28, 29),
    PHARMACY(PlaceCategory.PHARMACY, 1, 3, 10, 11, 13, 14);

    private final PlaceCategory category;
    private final int nameIndex;
    private final int facilityTypeIndex;
    private final int addressIndex;
    private final int phoneIndex;
    private final int longitudeIndex;
    private final int latitudeIndex;
    private final int minimumColumns;

    PlaceCsvFormat(
        PlaceCategory category,
        int nameIndex,
        int facilityTypeIndex,
        int addressIndex,
        int phoneIndex,
        int longitudeIndex,
        int latitudeIndex
    ) {
        this.category = category;
        this.nameIndex = nameIndex;
        this.facilityTypeIndex = facilityTypeIndex;
        this.addressIndex = addressIndex;
        this.phoneIndex = phoneIndex;
        this.longitudeIndex = longitudeIndex;
        this.latitudeIndex = latitudeIndex;
        this.minimumColumns = 1 + Math.max(
            Math.max(Math.max(nameIndex, facilityTypeIndex), Math.max(addressIndex, phoneIndex)),
            Math.max(longitudeIndex, latitudeIndex)
        );
    }

    public PlaceCategory category() {
        return category;
    }

    /**
     * CSV 한 행을 장소로 변환한다.
     *
     * @throws IllegalArgumentException 컬럼이 모자라거나 이름·주소·좌표가 유효하지 않은 경우
     */
    public PlaceCsvRow parse(String[] row) {
        if (row == null || row.length < minimumColumns) {
            throw new IllegalArgumentException("CSV 컬럼 수가 부족합니다");
        }
        String name = row[nameIndex].trim();
        String address = row[addressIndex].trim();
        if (name.isBlank() || address.isBlank()) {
            throw new IllegalArgumentException("이름 또는 주소가 비어 있습니다");
        }
        double longitude = Double.parseDouble(row[longitudeIndex].trim());
        double latitude = Double.parseDouble(row[latitudeIndex].trim());
        if (longitude < -180 || longitude > 180 || latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("좌표 범위를 벗어났습니다");
        }
        return new PlaceCsvRow(
            name,
            category,
            row[facilityTypeIndex].trim(),
            address,
            row[phoneIndex].trim(),
            longitude,
            latitude
        );
    }
}
