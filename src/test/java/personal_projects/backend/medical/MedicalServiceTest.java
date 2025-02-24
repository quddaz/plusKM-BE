package personal_projects.backend.medical;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.locationtech.jts.geom.Point;
import personal_projects.backend.domain.medical.domain.Medical;
import personal_projects.backend.domain.medical.domain.enumType.Medical_department;
import personal_projects.backend.domain.medical.dto.request.MedicalDTO;
import personal_projects.backend.domain.medical.dto.response.MedicalPageResponse;
import personal_projects.backend.domain.medical.repository.MedicalRepository;
import personal_projects.backend.domain.medical.service.MedicalService;
import personal_projects.backend.domain.place.domain.Place;
import personal_projects.backend.domain.place.domain.enumType.Place_type;
import personal_projects.backend.domain.place.service.PlaceService;
import personal_projects.backend.domain.user.domain.User;
import personal_projects.backend.domain.user.domain.enumType.Oauth_type;
import personal_projects.backend.domain.user.domain.enumType.Role;
import personal_projects.backend.domain.user.service.UserService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalServiceTest {
    @InjectMocks
    private MedicalService medicalService;

    @Mock
    private MedicalRepository medicalRepository;
    @Mock
    private PlaceService placeService;
    @Mock
    private UserService userService;

    private User user;
    private Place place;
    private Medical medical;
    private MedicalDTO medicalDTO;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .id(1L)
            .name("테스트 유저")
            .oauth_type(Oauth_type.GOOGLE)
            .socialId("123456789")
            .email("test@example.com")
            .role(Role.USER)
            .build();

        place = Place.builder()
            .id(1L)
            .name("테스트 병원")
            .place_type(Place_type.병원)
            .address("서울 강남구")
            .tel("02-123-4567")
            .coordinate(new GeometryFactory().createPoint(new Coordinate(37.5665, 126.9780)))
            .build();

        medicalDTO = MedicalDTO.builder()
            .content("진료 내용")
            .department("내과")
            .medical_fee(50000L)
            .place_id(1L)
            .build();

        medical = MedicalDTO.toEntity(medicalDTO, place, user);
    }

    @Test
    void saveMedical_성공() {
        // given
        when(userService.findById(anyLong())).thenReturn(user);
        when(placeService.findById(anyLong())).thenReturn(place);
        when(medicalRepository.save(any(Medical.class))).thenReturn(medical);

        // when
        medicalService.saveMedical(medicalDTO, 1L);

        // then
        verify(medicalRepository, times(1)).save(any(Medical.class));
    }

    @Test
    void deleteMedical_본인_작성_성공() {
        // given
        when(medicalRepository.findById(anyLong())).thenReturn(Optional.of(medical));

        // when
        medicalService.deleteMedical(1L, 1L);

        // then
        verify(medicalRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteMedical_본인_아님_예외() {
        // given
        User anotherUser = User.builder().id(2L).build();
        Medical anotherMedical = Medical.builder()
            .id(2L)
            .content("다른 사용자 진료 기록")
            .department(Medical_department.내과)
            .medical_fee(50000L)
            .place(place)
            .user(anotherUser)
            .build();
        when(medicalRepository.findById(anyLong())).thenReturn(Optional.of(anotherMedical));

        // when & then
        assertThrows(IllegalArgumentException.class, () -> medicalService.deleteMedical(1L, 1L));
    }

    @Test
    void findMedicalByDepartment_성공() {
        // given
        List<MedicalPageResponse> responses = List.of(
            new MedicalPageResponse(1L, "진료 내용", "INTERNAL_MEDICINE", 50000L, "테스트 병원")
        );
        when(medicalRepository.findMedicalByDepartment(anyString(), anyLong(), anyLong(), anyInt()))
            .thenReturn(responses);

        // when
        List<MedicalPageResponse> result = medicalService.findMedicalByDepartment("INTERNAL_MEDICINE", 1L, 0L, 10);

        // then
        assertEquals(1, result.size());
        assertEquals("진료 내용", result.get(0).content());
    }
}
