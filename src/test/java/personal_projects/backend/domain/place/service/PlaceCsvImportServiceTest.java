package personal_projects.backend.domain.place.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import personal_projects.backend.domain.place.config.PlaceCsvProperties;
import personal_projects.backend.domain.place.dto.response.PlaceCsvImportResponse;
import personal_projects.backend.domain.place.importer.PlaceCsvReader;
import personal_projects.backend.domain.place.repository.PlaceCsvStageRepository;

class PlaceCsvImportServiceTest {

    private static final PlaceCsvProperties PROPERTIES = new PlaceCsvProperties(
        "place-soft-delete-hospitals.csv", "place-soft-delete-pharmacies.csv", 10, false
    );

    @Test
    void csv_반영은_스테이징_후_갱신_추가_비활성화_순서로_병합한다() {
        PlaceCsvStageRepository repository = mock(PlaceCsvStageRepository.class);
        when(repository.countStaged()).thenReturn(2);
        when(repository.countActivePlaces()).thenReturn(2);
        when(repository.countInactivePlaces()).thenReturn(3);
        PlaceCsvImportService service = new PlaceCsvImportService(repository, new PlaceCsvReader(), PROPERTIES);

        PlaceCsvImportResponse result = service.importAll();

        assertEquals(1, result.hospitals());
        assertEquals(1, result.pharmacies());
        assertEquals(3, result.inactivePlaces());
        InOrder order = inOrder(repository);
        order.verify(repository).ensureSpatialIndex();
        order.verify(repository).createStageTable();
        order.verify(repository, atLeastOnce()).saveAllToStage(anyList());
        order.verify(repository).updateChangedFromStage();
        order.verify(repository).insertNewFromStage();
        order.verify(repository).deactivateMissingFromStage();
        order.verify(repository).dropStageTable();
    }

    @Test
    void 스테이징_행이_지나치게_적으면_운영_테이블을_변경하지_않는다() {
        PlaceCsvStageRepository repository = mock(PlaceCsvStageRepository.class);
        when(repository.countStaged()).thenReturn(2);
        when(repository.countActivePlaces()).thenReturn(100);
        PlaceCsvImportService service = new PlaceCsvImportService(repository, new PlaceCsvReader(), PROPERTIES);

        assertThrows(IllegalStateException.class, service::importAll);
        verify(repository, never()).updateChangedFromStage();
        verify(repository, never()).insertNewFromStage();
        verify(repository, never()).deactivateMissingFromStage();
        verify(repository).dropStageTable();
    }
}
