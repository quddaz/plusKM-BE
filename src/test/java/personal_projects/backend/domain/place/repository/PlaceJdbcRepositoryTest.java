package personal_projects.backend.domain.place.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import personal_projects.backend.common.geo.GeoBoundingBox;
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
        assertTrue(sql.getValue().contains("active = true"));
        assertTrue(sql.getValue().contains("latitude BETWEEN ? AND ?"));
        assertTrue(sql.getValue().contains("longitude BETWEEN ? AND ?"));
        assertTrue(sql.getValue().contains("ASIN"));
        assertTrue(sql.getValue().contains("ORDER BY distance_meters, id"));
        assertFalse(sql.getValue().contains("MBRContains"));
        assertFalse(sql.getValue().contains("ST_Distance_Sphere"));
    }

    @Test
    void 중심점과_경계_값을_쿼리_순서대로_전달한다() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        PlaceJdbcRepository repository = new PlaceJdbcRepository(jdbcTemplate);
        NearbyPlaceSearchRequest request = new NearbyPlaceSearchRequest(
            127.1048549, 37.4003268, 3.0, PlaceCategory.HOSPITAL
        );

        repository.findNearby(request);

        ArgumentCaptor<Object[]> parameters = ArgumentCaptor.forClass(Object[].class);
        verify(jdbcTemplate).query(ArgumentMatchers.anyString(),
            ArgumentMatchers.<RowMapper<NearbyPlaceResponse>>any(), parameters.capture());
        GeoBoundingBox bounds = GeoBoundingBox.aroundKilometers(
            request.longitude(), request.latitude(), request.radiusKilometers());
        assertEquals(request.latitude(), parameters.getValue()[0]);
        assertEquals(request.latitude(), parameters.getValue()[1]);
        assertEquals(request.longitude(), parameters.getValue()[2]);
        assertEquals(bounds.minLatitude(), parameters.getValue()[3]);
        assertEquals(bounds.maxLatitude(), parameters.getValue()[4]);
        assertEquals(bounds.minLongitude(), parameters.getValue()[5]);
        assertEquals(bounds.maxLongitude(), parameters.getValue()[6]);
        assertEquals(PlaceCategory.HOSPITAL.name(), parameters.getValue()[7]);
        assertEquals(3_000.0, parameters.getValue()[8]);
    }
}
