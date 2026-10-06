package personal_projects.backend.domain.place.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import personal_projects.backend.domain.place.dto.request.NearbyPlaceSearchRequest;
import personal_projects.backend.domain.place.dto.response.NearbyPlaceResponse;
import personal_projects.backend.domain.place.entity.PlaceCategory;

class PlaceJdbcRepositoryTest {

    @Test
    void 반경_안의_검색_결과를_개수로_자르지_않는다() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        PlaceJdbcRepository repository = new PlaceJdbcRepository(jdbcTemplate);

        repository.findNearby(new NearbyPlaceSearchRequest(127.1112, 37.3948, 3.0,
            PlaceCategory.ALL));

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).query(sql.capture(),
            ArgumentMatchers.<RowMapper<NearbyPlaceResponse>>any(), any(Object[].class));
        assertFalse(sql.getValue().toUpperCase().contains("LIMIT"));
    }
}
