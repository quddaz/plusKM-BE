package personal_projects.backend.domain.place.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import personal_projects.backend.domain.place.dto.PlaceCsvRow;
import personal_projects.backend.domain.place.entity.PlaceCategory;

class PlaceCsvStageRepositoryTest {

    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final PlaceCsvStageRepository repository = new PlaceCsvStageRepository(jdbc);

    @Test
    void 병합_쿼리는_실제_삭제_없이_비활성_처리한다() {
        repository.updateChangedFromStage();
        repository.insertNewFromStage();
        repository.deactivateMissingFromStage();

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(jdbc, times(3)).update(sql.capture());
        List<String> queries = sql.getAllValues();
        assertTrue(queries.get(0).contains("UPDATE place p JOIN place_csv_stage"));
        assertTrue(queries.get(1).contains("INSERT INTO place"));
        assertTrue(queries.get(2).contains("SET p.active = false"));
        assertFalse(queries.stream().anyMatch(query -> query.toUpperCase().contains("DELETE")));
    }

    @Test
    void 스테이징_저장은_스테이징_테이블에만_쓴다() {
        repository.saveAllToStage(List.of(new PlaceCsvRow(
            "병원", PlaceCategory.HOSPITAL, "의원", "주소", "010", 127.1, 37.4
        )));

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(jdbc).batchUpdate(sql.capture(), any(BatchPreparedStatementSetter.class));
        assertTrue(sql.getValue().contains("INSERT INTO place_csv_stage"));
    }

    @Test
    void 빈_배치는_저장하지_않는다() {
        repository.saveAllToStage(List.of());

        verify(jdbc, never()).batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
    }

    @Test
    void 스테이징_테이블은_임시_테이블로_만들고_지운다() {
        repository.createStageTable();
        repository.dropStageTable();

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(jdbc, times(2)).execute(sql.capture());
        assertTrue(sql.getAllValues().get(0).contains("CREATE TEMPORARY TABLE place_csv_stage"));
        assertTrue(sql.getAllValues().get(1).equals("DROP TEMPORARY TABLE IF EXISTS place_csv_stage"));
    }
}
