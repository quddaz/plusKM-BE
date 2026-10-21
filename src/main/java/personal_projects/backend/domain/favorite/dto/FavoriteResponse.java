package personal_projects.backend.domain.favorite.dto;

import personal_projects.backend.domain.emergency.entity.Emergency;
import personal_projects.backend.domain.favorite.entity.Favorite;
import personal_projects.backend.domain.place.entity.Place;

public record FavoriteResponse(
    Long id,
    String type,
    Long targetId,
    String hpid,
    String name,
    String address,
    String phoneNumber,
    String category,
    String facilityType,
    boolean active,
    double longitude,
    double latitude
) {

    public static FavoriteResponse from(Favorite favorite) {
        Emergency emergency = favorite.getEmergency();
        if (emergency != null) {
            return new FavoriteResponse(
                favorite.getId(), "EMERGENCY", emergency.getId(), emergency.getHpid(),
                emergency.getName(), emergency.getAddress(), emergency.getPhoneNumber(),
                null, null, emergency.isActive(),
                emergency.getCoordinate().getX(), emergency.getCoordinate().getY()
            );
        }
        Place place = favorite.getPlace();
        return new FavoriteResponse(
            favorite.getId(), "PLACE", place.getId(), null,
            place.getName(), place.getAddress(), place.getPhoneNumber(),
            place.getCategory().name(), place.getFacilityType(), place.isActive(),
            place.getCoordinate().getX(), place.getCoordinate().getY()
        );
    }
}
