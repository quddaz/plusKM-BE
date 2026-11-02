package personal_projects.backend.domain.favorite.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import personal_projects.backend.domain.emergency.entity.Emergency;
import personal_projects.backend.domain.emergency.repository.jpa.EmergencyJpaRepository;
import personal_projects.backend.domain.favorite.entity.Favorite;
import personal_projects.backend.domain.favorite.repository.FavoriteRepository;
import personal_projects.backend.domain.place.entity.Place;
import personal_projects.backend.domain.place.entity.PlaceCategory;
import personal_projects.backend.domain.place.repository.PlaceJpaRepository;
import personal_projects.backend.domain.user.entity.User;
import personal_projects.backend.domain.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @InjectMocks private FavoriteService service;
    @Mock private FavoriteRepository favoriteRepository;
    @Mock private EmergencyJpaRepository emergencyRepository;
    @Mock private PlaceJpaRepository placeRepository;
    @Mock private UserService userService;

    private final GeometryFactory geometryFactory = new GeometryFactory();

    @Test
    void 로그인_사용자가_응급실을_저장하고_중복_요청은_기존_기록을_반환한다() {
        User user = User.builder().id(1L).build();
        Emergency emergency = Emergency.builder().id(2L).hpid("A123")
            .name("응급실").active(true).coordinate(geometryFactory.createPoint(new Coordinate(127, 37)))
            .build();
        when(favoriteRepository.findByUser_IdAndEmergency_Id(1L, 2L))
            .thenReturn(Optional.empty(), Optional.of(Favorite.emergency(user, emergency)));
        when(emergencyRepository.findById(2L)).thenReturn(Optional.of(emergency));
        when(userService.findById(1L)).thenReturn(user);
        when(favoriteRepository.save(any(Favorite.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.addEmergency(1L, 2L).hpid()).isEqualTo("A123");
        assertThat(service.addEmergency(1L, 2L).type()).isEqualTo("EMERGENCY");
        verify(favoriteRepository).save(any(Favorite.class));
    }

    @Test
    void 비활성_장소는_새로_저장할_수_없지만_기존_즐겨찾기에는_남는다() {
        User user = User.builder().id(1L).build();
        Place place = Place.builder().id(3L).name("병원").category(PlaceCategory.HOSPITAL)
            .active(false).coordinate(geometryFactory.createPoint(new Coordinate(127, 37))).build();
        when(favoriteRepository.findByUser_IdAndPlace_Id(1L, 3L)).thenReturn(Optional.empty());
        when(placeRepository.findById(3L)).thenReturn(Optional.of(place));

        assertThatThrownBy(() -> service.addPlace(1L, 3L))
            .isInstanceOf(NoSuchElementException.class);
        verify(favoriteRepository, never()).save(any(Favorite.class));

        when(favoriteRepository.findAllByUser_IdOrderByIdDesc(1L))
            .thenReturn(List.of(Favorite.place(user, place)));
        assertThat(service.findAll(1L).getFirst().active()).isFalse();
    }

    @Test
    void 다른_사용자의_즐겨찾기는_삭제하지_않는다() {
        when(favoriteRepository.findByUser_IdAndEmergency_Id(1L, 5L)).thenReturn(Optional.empty());
        service.removeEmergency(1L, 5L);
        verify(favoriteRepository, never()).delete(any(Favorite.class));
    }
}
