package personal_projects.backend.domain.place.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import personal_projects.backend.domain.place.domain.Place;
import personal_projects.backend.domain.place.repository.PlaceRepository;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
@Slf4j
public class PlaceService {

    private final PlaceRepository placeRepository;

    public Place findById(Long id) {
        return placeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 id의 place가 없습니다."));
    }
}
