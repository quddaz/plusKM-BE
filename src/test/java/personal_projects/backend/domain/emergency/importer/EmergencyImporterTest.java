package personal_projects.backend.domain.emergency.importer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import personal_projects.backend.domain.emergency.client.EmergencyApiClient;
import personal_projects.backend.domain.emergency.client.EmergencyFacilityData;
import personal_projects.backend.domain.emergency.entity.Emergency;
import personal_projects.backend.domain.emergency.repository.EmergencyRepository;

@ExtendWith(MockitoExtension.class)
class EmergencyImporterTest {

    @InjectMocks private EmergencyImporter importer;
    @Mock private EmergencyApiClient apiClient;
    @Mock private EmergencyRepository repository;
    @Mock private GeometryFactory geometryFactory;

    @Test
    void 비활성_응급실이_다시_나타나면_기존_ID로_재활성화한다() {
        Emergency existing = Emergency.builder().id(7L).hpid("A123").active(false)
            .name("기존 응급실").build();
        when(apiClient.fetchFacilities()).thenReturn(List.of(
            new EmergencyFacilityData("A123", "변경된 응급실", "서울", "02-1234-5678", 127, 37)
        ));
        when(repository.findAll()).thenReturn(List.of(existing));
        when(geometryFactory.createPoint(org.mockito.ArgumentMatchers.any(Coordinate.class)))
            .thenAnswer(invocation -> new GeometryFactory().createPoint((Coordinate) invocation.getArgument(0)));

        importer.importAll();

        assertThat(existing.getId()).isEqualTo(7L);
        assertThat(existing.isActive()).isTrue();
        assertThat(existing.getName()).isEqualTo("변경된 응급실");
        verify(repository, times(2)).saveAll(List.of(existing));
    }
}
