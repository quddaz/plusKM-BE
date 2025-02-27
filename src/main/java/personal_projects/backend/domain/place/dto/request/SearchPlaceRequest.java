package personal_projects.backend.domain.place.dto.request;

import lombok.Builder;
import personal_projects.backend.domain.place.dto.Search_Type;

@Builder
public record SearchPlaceRequest(
    double longitude,
    double latitude,
    double bufferDistance,
    Search_Type searchType
) {
}
