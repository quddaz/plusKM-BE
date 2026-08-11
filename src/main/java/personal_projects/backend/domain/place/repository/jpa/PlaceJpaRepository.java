package personal_projects.backend.domain.place.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import personal_projects.backend.domain.place.entity.Place;

import java.util.List;

public interface PlaceJpaRepository extends JpaRepository<Place, Long> {

    @Query("SELECT p FROM Place p WHERE CONCAT(TRIM(p.name), '_', TRIM(p.address)) IN :keys")
    List<Place> findAllByUniqueKeyIn(@Param("keys") List<String> keys);

    @Modifying
    @Query("UPDATE Place p SET p.active = false")
    void deactivateAll();

    @Modifying
    @Query("DELETE FROM Place p WHERE p.active = false")
    void deleteInactivePlaces();
}
