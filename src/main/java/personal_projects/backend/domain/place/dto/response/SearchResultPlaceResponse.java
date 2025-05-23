package personal_projects.backend.domain.place.dto.response;

import lombok.Builder;
@Builder
public record SearchResultPlaceResponse(
    Long id,
    String name,
    String address,
    String tel,
    double x,
    double y
) {
}
