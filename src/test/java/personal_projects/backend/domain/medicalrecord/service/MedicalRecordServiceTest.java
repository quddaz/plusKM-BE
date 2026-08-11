package personal_projects.backend.domain.medicalrecord.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import personal_projects.backend.domain.medicalrecord.dto.request.CreateMedicalRecordRequest;
import personal_projects.backend.domain.medicalrecord.dto.response.MedicalRecordSummaryResponse;
import personal_projects.backend.domain.medicalrecord.entity.MedicalRecord;
import personal_projects.backend.domain.medicalrecord.repository.MedicalRecordRepository;
import personal_projects.backend.domain.medicalrecord.type.MedicalDepartment;
import personal_projects.backend.domain.place.entity.Place;
import personal_projects.backend.domain.place.service.PlaceService;
import personal_projects.backend.domain.place.type.PlaceType;
import personal_projects.backend.domain.user.entity.User;
import personal_projects.backend.domain.user.service.UserService;
import personal_projects.backend.domain.user.type.OAuthProvider;
import personal_projects.backend.domain.user.type.Role;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicalRecordServiceTest {
    @InjectMocks
    private MedicalRecordService medicalRecordService;

    @Mock
    private MedicalRecordRepository medicalRecordRepository;
    @Mock
    private PlaceService placeService;
    @Mock
    private UserService userService;

    private User user;
    private Place place;
    private MedicalRecord medicalRecord;
    private CreateMedicalRecordRequest request;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .id(1L)
            .name("테스트 유저")
            .oauthProvider(OAuthProvider.GOOGLE)
            .socialId("123456789")
            .email("test@example.com")
            .role(Role.USER)
            .build();

        place = Place.builder()
            .id(1L)
            .name("테스트 병원")
            .placeType(PlaceType.병원)
            .address("서울 강남구")
            .phoneNumber("02-123-4567")
            .coordinate(new GeometryFactory().createPoint(new Coordinate(37.5665, 126.9780)))
            .build();

        request = CreateMedicalRecordRequest.builder()
            .description("진료 내용")
            .department("내과")
            .medicalFee(50000L)
            .placeId(1L)
            .build();

        medicalRecord = request.toEntity(place, user);
    }

    @Test
    void createMedicalRecord_성공() {
        when(userService.findById(anyLong())).thenReturn(user);
        when(placeService.findById(anyLong())).thenReturn(place);
        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(medicalRecord);

        medicalRecordService.createMedicalRecord(request, 1L);

        verify(medicalRecordRepository, times(1)).save(any(MedicalRecord.class));
    }

    @Test
    void deleteMedicalRecord_본인작성_성공() {
        when(medicalRecordRepository.findById(anyLong())).thenReturn(Optional.of(medicalRecord));

        medicalRecordService.deleteMedicalRecord(1L, 1L);

        verify(medicalRecordRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteMedicalRecord_본인아님_예외() {
        User anotherUser = User.builder().id(2L).build();
        MedicalRecord anotherMedicalRecord = MedicalRecord.builder()
            .id(2L)
            .description("다른 사용자 진료 기록")
            .department(MedicalDepartment.내과)
            .medicalFee(50000L)
            .place(place)
            .user(anotherUser)
            .build();
        when(medicalRecordRepository.findById(anyLong())).thenReturn(Optional.of(anotherMedicalRecord));

        assertThrows(IllegalArgumentException.class, () -> medicalRecordService.deleteMedicalRecord(1L, 1L));
    }

    @Test
    void findMedicalRecords_성공() {
        List<MedicalRecordSummaryResponse> responses = List.of(
            new MedicalRecordSummaryResponse(1L, "진료 내용", "INTERNAL_MEDICINE", 50000L, "테스트 유저")
        );
        when(medicalRecordRepository.findMedicalRecords(anyString(), anyLong(), anyLong(), anyInt()))
            .thenReturn(responses);

        List<MedicalRecordSummaryResponse> result =
            medicalRecordService.findMedicalRecords("INTERNAL_MEDICINE", 1L, 0L, 10);

        assertEquals(1, result.size());
        assertEquals("진료 내용", result.get(0).description());
    }
}
