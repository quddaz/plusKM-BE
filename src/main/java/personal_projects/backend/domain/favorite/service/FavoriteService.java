package personal_projects.backend.domain.favorite.service;

import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import personal_projects.backend.domain.emergency.entity.Emergency;
import personal_projects.backend.domain.emergency.repository.jpa.EmergencyJpaRepository;
import personal_projects.backend.domain.favorite.dto.FavoriteResponse;
import personal_projects.backend.domain.favorite.entity.Favorite;
import personal_projects.backend.domain.favorite.repository.FavoriteRepository;
import personal_projects.backend.domain.place.entity.Place;
import personal_projects.backend.domain.place.repository.PlaceJpaRepository;
import personal_projects.backend.domain.user.entity.User;
import personal_projects.backend.domain.user.service.UserService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final EmergencyJpaRepository emergencyRepository;
    private final PlaceJpaRepository placeRepository;
    private final UserService userService;

    public List<FavoriteResponse> findAll(Long userId) {
        return favoriteRepository.findAllByUser_IdOrderByIdDesc(userId).stream()
            .map(FavoriteResponse::from)
            .toList();
    }

    @Transactional
    public FavoriteResponse addEmergency(Long userId, Long emergencyId) {
        return favoriteRepository.findByUser_IdAndEmergency_Id(userId, emergencyId)
            .map(FavoriteResponse::from)
            .orElseGet(() -> {
                Emergency emergency = emergencyRepository.findById(emergencyId)
                    .filter(Emergency::isActive)
                    .orElseThrow(() -> new NoSuchElementException("활성 응급실을 찾을 수 없습니다."));
                User user = userService.findById(userId);
                return FavoriteResponse.from(favoriteRepository.save(Favorite.emergency(user, emergency)));
            });
    }

    @Transactional
    public FavoriteResponse addPlace(Long userId, Long placeId) {
        return favoriteRepository.findByUser_IdAndPlace_Id(userId, placeId)
            .map(FavoriteResponse::from)
            .orElseGet(() -> {
                Place place = placeRepository.findById(placeId)
                    .filter(Place::isActive)
                    .orElseThrow(() -> new NoSuchElementException("활성 병원·약국을 찾을 수 없습니다."));
                User user = userService.findById(userId);
                return FavoriteResponse.from(favoriteRepository.save(Favorite.place(user, place)));
            });
    }

    @Transactional
    public void removeEmergency(Long userId, Long emergencyId) {
        favoriteRepository.findByUser_IdAndEmergency_Id(userId, emergencyId)
            .ifPresent(favoriteRepository::delete);
    }

    @Transactional
    public void removePlace(Long userId, Long placeId) {
        favoriteRepository.findByUser_IdAndPlace_Id(userId, placeId)
            .ifPresent(favoriteRepository::delete);
    }
}
