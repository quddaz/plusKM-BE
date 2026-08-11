package personal_projects.backend.domain.place.dto.response;

import lombok.Builder;

@Builder
public record PlaceDetailResponse(
    Long id,
    String name,
    String address,
    String phoneNumber,
    String placeType,

    boolean bookmarked
) {
}
