package personal_projects.backend.domain.medical.repository.jdbc;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import personal_projects.backend.domain.medical.dto.response.MedicalPageResponse;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MedicalJdbcRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public List<MedicalPageResponse> findMedicalByDepartment(
        String department,
        Long placeId,
        Long lastId,
        int size
    ) {
        StringBuilder sql = new StringBuilder("""
            SELECT m.id, m.content, m.department, m.medical_fee, u.name
            FROM medical m
            INNER JOIN users u ON u.id = m.user_id
            WHERE m.place_id = :placeId
              AND m.id > :lastId
            """);

        MapSqlParameterSource parameters = new MapSqlParameterSource()
            .addValue("placeId", placeId)
            .addValue("lastId", lastId)
            .addValue("size", size);

        if (department != null && !department.isBlank()) {
            sql.append(" AND m.department = :department");
            parameters.addValue("department", department);
        }

        sql.append(" ORDER BY m.id ASC LIMIT :size");

        return jdbcTemplate.query(sql.toString(), parameters, (resultSet, rowNumber) ->
            new MedicalPageResponse(
                resultSet.getLong("id"),
                resultSet.getString("content"),
                resultSet.getString("department"),
                resultSet.getLong("medical_fee"),
                resultSet.getString("name")
            ));
    }
}
