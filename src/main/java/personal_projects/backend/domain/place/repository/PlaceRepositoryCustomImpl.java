package personal_projects.backend.domain.place.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import personal_projects.backend.domain.place.repository.PlaceRepositoryCustom;

@Repository
@RequiredArgsConstructor
public class PlaceRepositoryCustomImpl extends PlaceRepositoryCustom {
    private final JPAQueryFactory queryFactory;


}
