package personal_projects.backend.domain.place.importer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import personal_projects.backend.domain.place.dto.PlaceCsvRow;
import personal_projects.backend.domain.place.entity.PlaceCategory;

class PlaceCsvFormatTest {

    private static String[] pharmacyRow(String name, String address, String longitude, String latitude) {
        String[] row = new String[15];
        Arrays.fill(row, "");
        row[1] = name;
        row[3] = "약국";
        row[10] = address;
        row[11] = "02-000-0000";
        row[13] = longitude;
        row[14] = latitude;
        return row;
    }

    @Test
    void 약국_행을_장소로_변환한다() {
        PlaceCsvRow row = PlaceCsvFormat.PHARMACY.parse(
            pharmacyRow(" 테스트약국 ", "서울 테스트로 2", "127.1", "37.4")
        );

        assertEquals("테스트약국", row.name());
        assertEquals(PlaceCategory.PHARMACY, row.category());
        assertEquals("약국", row.facilityType());
        assertEquals(127.1, row.longitude());
        assertEquals(37.4, row.latitude());
    }

    @Test
    void 컬럼이_모자란_행은_거부한다() {
        assertThrows(IllegalArgumentException.class,
            () -> PlaceCsvFormat.HOSPITAL.parse(pharmacyRow("병원", "주소", "127.1", "37.4")));
    }

    @Test
    void 이름이나_주소가_비면_거부한다() {
        assertThrows(IllegalArgumentException.class,
            () -> PlaceCsvFormat.PHARMACY.parse(pharmacyRow(" ", "주소", "127.1", "37.4")));
        assertThrows(IllegalArgumentException.class,
            () -> PlaceCsvFormat.PHARMACY.parse(pharmacyRow("약국", "", "127.1", "37.4")));
    }

    @Test
    void 좌표가_숫자가_아니거나_범위를_벗어나면_거부한다() {
        assertThrows(IllegalArgumentException.class,
            () -> PlaceCsvFormat.PHARMACY.parse(pharmacyRow("약국", "주소", "abc", "37.4")));
        assertThrows(IllegalArgumentException.class,
            () -> PlaceCsvFormat.PHARMACY.parse(pharmacyRow("약국", "주소", "181", "37.4")));
        assertThrows(IllegalArgumentException.class,
            () -> PlaceCsvFormat.PHARMACY.parse(pharmacyRow("약국", "주소", "127.1", "-91")));
    }
}
