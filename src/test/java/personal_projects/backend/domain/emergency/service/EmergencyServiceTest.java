package personal_projects.backend.domain.emergency.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import personal_projects.backend.domain.emergency.dto.response.EmergencyDetailResponse;
import personal_projects.backend.domain.emergency.entity.Emergency;
import java.util.NoSuchElementException;
import personal_projects.backend.domain.emergency.repository.EmergencyRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmergencyServiceTest {

    @InjectMocks
    private EmergencyService emergencyService;

    @Mock
    private EmergencyRepository emergencyRepository;

    @Test
    void findById_성공() {
        Emergency emergency = Emergency.builder()
            .id(1L)
            .name("테스트 장소")
            .address("서울 강남구")
            .phoneNumber("02-123-4567")
            .coordinate(new GeometryFactory().createPoint(new Coordinate(37.5665, 126.9780)))
            .build();

        when(emergencyRepository.findById(1L)).thenReturn(Optional.of(emergency));

        Emergency result = emergencyService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("테스트 장소", result.getName());
    }

    @Test
    void findById_존재하지않음_예외() {
        when(emergencyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> emergencyService.findById(1L));
    }

    @Test
    void findEmergencyDetail_성공() {
        EmergencyDetailResponse response = EmergencyDetailResponse.builder()
            .id(1L)
            .name("테스트 장소")
            .address("서울 강남구")
            .phoneNumber("02-123-4567")
            .build();

        when(emergencyRepository.findEmergencyDetail(1L)).thenReturn(response);

        EmergencyDetailResponse result = emergencyService.findEmergencyDetail(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("테스트 장소", result.name());
    }
}
