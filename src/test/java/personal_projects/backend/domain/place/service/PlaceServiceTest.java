package personal_projects.backend.domain.place.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import personal_projects.backend.domain.place.dto.response.BookmarkedPlaceResponse;
import personal_projects.backend.domain.place.dto.response.PlaceDetailResponse;
import personal_projects.backend.domain.place.entity.Place;
import personal_projects.backend.common.exception.BusinessException;
import personal_projects.backend.domain.place.repository.PlaceRepository;
import personal_projects.backend.domain.place.type.PlaceType;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaceServiceTest {

    @InjectMocks
    private PlaceService placeService;

    @Mock
    private PlaceRepository placeRepository;

    @Test
    void findById_성공() {
        Place place = Place.builder()
            .id(1L)
            .name("테스트 장소")
            .placeType(PlaceType.병원)
            .address("서울 강남구")
            .phoneNumber("02-123-4567")
            .coordinate(new GeometryFactory().createPoint(new Coordinate(37.5665, 126.9780)))
            .build();

        when(placeRepository.findById(1L)).thenReturn(Optional.of(place));

        Place result = placeService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("테스트 장소", result.getName());
    }

    @Test
    void findById_존재하지않음_예외() {
        when(placeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> placeService.findById(1L));
    }

    @Test
    void findPlaceDetail_성공() {
        PlaceDetailResponse response = PlaceDetailResponse.builder()
            .id(1L)
            .name("테스트 장소")
            .address("서울 강남구")
            .phoneNumber("02-123-4567")
            .placeType("병원")
            .bookmarked(true)
            .build();

        when(placeRepository.findPlaceDetail(1L, 1L)).thenReturn(response);

        PlaceDetailResponse result = placeService.findPlaceDetail(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("테스트 장소", result.name());
        assertEquals("병원", result.placeType());
        assertTrue(result.bookmarked());
    }

    @Test
    void findBookmarkedPlacesByUserId_북마크존재() {
        List<BookmarkedPlaceResponse> responses = List.of(
            BookmarkedPlaceResponse.builder()
                .id(1L)
                .name("테스트 장소")
                .address("서울 강남구")
                .phoneNumber("02-123-4567")
                .placeType("병원")
                .build()
        );

        when(placeRepository.findBookmarkedPlacesByUserId(1L)).thenReturn(responses);

        List<BookmarkedPlaceResponse> result = placeService.findBookmarkedPlacesByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("테스트 장소", result.get(0).name());
    }

    @Test
    void findBookmarkedPlacesByUserId_북마크없음() {
        when(placeRepository.findBookmarkedPlacesByUserId(1L)).thenReturn(Collections.emptyList());

        List<BookmarkedPlaceResponse> result = placeService.findBookmarkedPlacesByUserId(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
