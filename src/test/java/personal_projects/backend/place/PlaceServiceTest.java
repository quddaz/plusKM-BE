package personal_projects.backend.place;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import personal_projects.backend.domain.place.domain.Place;
import personal_projects.backend.domain.place.domain.enumType.Place_type;
import personal_projects.backend.domain.place.dto.response.SearchBookMarkPlaceResponse;
import personal_projects.backend.domain.place.dto.response.SearchDetailPlaceResponse;
import personal_projects.backend.domain.place.exception.PlaceNotFoundException;
import personal_projects.backend.domain.place.repository.PlaceRepository;
import personal_projects.backend.domain.place.service.PlaceService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaceServiceTest {

    @InjectMocks
    private PlaceService placeService;

    @Mock
    private PlaceRepository placeRepository;

    @Test
    void findById_성공() {
        // given
        Place place = Place.builder()
            .id(1L)
            .name("테스트 장소")
            .place_type(Place_type.병원)
            .address("서울 강남구")
            .tel("02-123-4567")
            .coordinate(new GeometryFactory().createPoint(new Coordinate(37.5665, 126.9780)))
            .build();

        when(placeRepository.findById(1L)).thenReturn(Optional.of(place));

        // when
        Place result = placeService.findById(1L);

        // then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("테스트 장소", result.getName());
    }

    @Test
    void findById_존재하지않음_예외() {
        // given
        when(placeRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(PlaceNotFoundException.class, () -> placeService.findById(1L));
    }

    @Test
    void findPlaceDetailByPlaceId_성공() {
        // given
        SearchDetailPlaceResponse response = SearchDetailPlaceResponse.builder()
            .id(1L)
            .name("테스트 장소")
            .address("서울 강남구")
            .tel("02-123-4567")
            .place_type("병원")
            .bookmarked(true)
            .build();

        when(placeRepository.findPlaceDetailByPlaceId(1L, 1L)).thenReturn(response);

        // when
        SearchDetailPlaceResponse result = placeService.findPlaceDetailByPlaceId(1L, 1L);

        // then
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("테스트 장소", result.name());
        assertEquals("병원", result.place_type());
        assertTrue(result.bookmarked());
    }

    @Test
    void findBookMarkPlacesByUserId_북마크존재() {
        // given
        List<SearchBookMarkPlaceResponse> responses = List.of(
            SearchBookMarkPlaceResponse.builder()
                .id(1L)
                .name("테스트 장소")
                .address("서울 강남구")
                .tel("02-123-4567")
                .place_type("병원")
                .build()
        );

        when(placeRepository.findBookMarkPlacesByUserId(1L)).thenReturn(responses);

        // when
        List<SearchBookMarkPlaceResponse> result = placeService.findBookMarkPlacesByUserId(1L);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("테스트 장소", result.get(0).name());
    }

    @Test
    void findBookMarkPlacesByUserId_북마크없음() {
        // given
        when(placeRepository.findBookMarkPlacesByUserId(1L)).thenReturn(Collections.emptyList());

        // when
        List<SearchBookMarkPlaceResponse> result = placeService.findBookMarkPlacesByUserId(1L);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
