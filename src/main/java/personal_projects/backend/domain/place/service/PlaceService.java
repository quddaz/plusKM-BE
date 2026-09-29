package personal_projects.backend.domain.place.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import personal_projects.backend.domain.place.dto.request.NearbyPlaceSearchRequest;
import personal_projects.backend.domain.place.dto.response.NearbyPlaceResponse;
import personal_projects.backend.domain.place.repository.PlaceJdbcRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private final PlaceJdbcRepository placeJdbcRepository;

    public List<NearbyPlaceResponse> findNearby(NearbyPlaceSearchRequest request) {
        return placeJdbcRepository.findNearby(request);
    }
}
