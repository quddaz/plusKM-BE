package personal_projects.backend.domain.place.dto.response;

import lombok.Builder;

@Builder
public record SearchBookMarkPlaceResponse(
    Long id,
    String name,
    String address,
    String tel,
    String place_type
) {
}
