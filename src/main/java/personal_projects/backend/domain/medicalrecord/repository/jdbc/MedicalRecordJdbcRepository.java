package personal_projects.backend.domain.medicalrecord.repository.jdbc;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import personal_projects.backend.domain.medicalrecord.dto.response.MedicalRecordSummaryResponse;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MedicalRecordJdbcRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public List<MedicalRecordSummaryResponse> findMedicalRecords(
        String department,
        Long placeId,
        Long lastId,
        int size
    ) {
        StringBuilder sql = new StringBuilder("""
            SELECT m.id, m.content AS description, m.department,
                   m.medical_fee, u.name AS user_name
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
            new MedicalRecordSummaryResponse(
                resultSet.getLong("id"),
                resultSet.getString("description"),
                resultSet.getString("department"),
                resultSet.getLong("medical_fee"),
                resultSet.getString("user_name")
            ));
    }
}
