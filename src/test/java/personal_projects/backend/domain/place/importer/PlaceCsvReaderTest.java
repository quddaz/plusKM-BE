package personal_projects.backend.domain.place.importer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import personal_projects.backend.domain.place.dto.PlaceCsvRow;

class PlaceCsvReaderTest {

    private final PlaceCsvReader reader = new PlaceCsvReader();

    @Test
    void 헤더를_건너뛰고_행을_배치로_넘긴다() {
        List<List<PlaceCsvRow>> batches = new ArrayList<>();

        PlaceCsvReadResult result = reader.read(
            "place-soft-delete-pharmacies.csv", PlaceCsvFormat.PHARMACY, 10, batches::add
        );

        assertEquals(new PlaceCsvReadResult(1, 0), result);
        assertEquals(1, batches.size());
        assertEquals("테스트약국", batches.get(0).get(0).name());
    }

    @Test
    void 형식이_맞지_않는_행은_건너뛰고_센다() {
        List<List<PlaceCsvRow>> batches = new ArrayList<>();

        // 약국 파일은 병원 형식보다 컬럼이 적어 모든 행이 건너뛰어진다.
        PlaceCsvReadResult result = reader.read(
            "place-soft-delete-pharmacies.csv", PlaceCsvFormat.HOSPITAL, 10, batches::add
        );

        assertEquals(new PlaceCsvReadResult(0, 1), result);
        assertEquals(0, batches.size());
    }

    @Test
    void 파일이_없으면_읽기_실패로_알린다() {
        assertThrows(IllegalStateException.class, () -> reader.read(
            "missing.csv", PlaceCsvFormat.PHARMACY, 10, batch -> { }
        ));
    }

    @Test
    void 저장_중_예외는_감싸지_않고_전파한다() {
        RuntimeException failure = new RuntimeException("db down");

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> reader.read(
            "place-soft-delete-pharmacies.csv", PlaceCsvFormat.PHARMACY, 10,
            batch -> { throw failure; }
        ));

        assertSame(failure, thrown);
    }

    @Test
    void 배치_크기는_1_이상이어야_한다() {
        assertThrows(IllegalArgumentException.class, () -> reader.read(
            "place-soft-delete-pharmacies.csv", PlaceCsvFormat.PHARMACY, 0, batch -> { }
        ));
    }
}
