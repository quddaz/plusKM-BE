package personal_projects.backend.domain.place.dto.response;

import lombok.Builder;

@Builder
public record SearchDetailPlaceResponse(
    Long id,
    String name,
    String address,
    String tel,
    String place_type,

    boolean bookmarked
) {
}
