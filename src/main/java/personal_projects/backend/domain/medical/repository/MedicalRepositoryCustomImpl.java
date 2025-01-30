package personal_projects.backend.domain.medical.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import personal_projects.backend.domain.medical.domain.enumType.Medical_department;
import personal_projects.backend.domain.medical.dto.response.MedicalPageResponse;

import java.util.List;

import static personal_projects.backend.domain.medical.domain.QMedical.medical;
import static personal_projects.backend.domain.place.domain.QPlace.place;
import static personal_projects.backend.domain.user.domain.QUser.user;

@RequiredArgsConstructor
public class MedicalRepositoryCustomImpl implements MedicalRepositoryCustom {
    private final JPAQueryFactory queryFactory;


    @Override
    public List<MedicalPageResponse> findMedicalByDepartment(String department, Long placeId, Long lastId, int size) {
        BooleanBuilder conditions = new BooleanBuilder();

        if (department != null) {
            conditions.and(medical.department.eq(Medical_department.valueOf(department)));
        }

        conditions.and(medical.id.gt(lastId))
            .and(place.id.eq(placeId));

        return queryFactory.select(Projections.constructor(MedicalPageResponse.class,
                medical.id,
                medical.content,
                medical.department.stringValue(),
                medical.medical_fee,
                user.name))
            .from(medical)
            .leftJoin(medical.user, user)
            .leftJoin(medical.place, place)
            .where(conditions)
            .limit(size)
            .fetch();
    }
}
