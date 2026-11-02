package personal_projects.backend.domain.favorite.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import personal_projects.backend.domain.favorite.entity.Favorite;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    @EntityGraph(attributePaths = {"emergency", "place"})
    List<Favorite> findAllByUser_IdOrderByIdDesc(Long userId);

    Optional<Favorite> findByUser_IdAndEmergency_Id(Long userId, Long emergencyId);

    Optional<Favorite> findByUser_IdAndPlace_Id(Long userId, Long placeId);
}
