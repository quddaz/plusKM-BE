package personal_projects.backend.domain.place.dto.response;

import org.locationtech.jts.geom.Point;

public record SearchResultPlaceResponse(
    Long id,
    String name,
    String address,
    String tel,
    double x,
    double y
) {
}
