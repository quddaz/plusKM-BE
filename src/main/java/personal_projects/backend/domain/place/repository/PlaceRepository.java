package personal_projects.backend.domain.place.repository;

import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import personal_projects.backend.domain.place.domain.Place;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long>, PlaceRepositoryCustom {

    // 대용량 데이터 스트리밍 조회 메서드
    @QueryHints(value = @QueryHint(name = org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE, value = "1000"))
    @Query("SELECT p FROM Place p")
    Stream<Place> streamAll();

    // 장소 ID로 장소 조회
    @Query("SELECT p FROM Place p WHERE CONCAT(TRIM(p.name), '_', TRIM(p.address)) IN :keys")
    List<Place> findAllByUniqueKeyIn(@Param("keys") List<String> keys);

    // 공공데이터 갱신 시 사용
    @Modifying
    @Query("UPDATE Place p SET p.active = false")
    void deactivateAll();

    // 비활성화된 장소 삭제
    @Modifying
    @Query("DELETE FROM Place p WHERE p.active = false")
    void deleteInactivePlaces();
}
