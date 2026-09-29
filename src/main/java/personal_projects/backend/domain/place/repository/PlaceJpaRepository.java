package personal_projects.backend.domain.place.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import personal_projects.backend.domain.place.entity.Place;

public interface PlaceJpaRepository extends JpaRepository<Place, Long> {
}
