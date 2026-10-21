리package personal_projects.backend.domain.favorite.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import personal_projects.backend.domain.emergency.entity.Emergency;
import personal_projects.backend.domain.place.entity.Place;
import personal_projects.backend.domain.user.entity.User;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "favorite", uniqueConstraints = {
    @UniqueConstraint(name = "uk_favorite_user_emergency", columnNames = {"user_id", "emergency_id"}),
    @UniqueConstraint(name = "uk_favorite_user_place", columnNames = {"user_id", "place_id"})
})
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emergency_id")
    private Emergency emergency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;

    private Favorite(User user, Emergency emergency, Place place) {
        this.user = user;
        this.emergency = emergency;
        this.place = place;
    }

    public static Favorite emergency(User user, Emergency emergency) {
        return new Favorite(user, emergency, null);
    }

    public static Favorite place(User user, Place place) {
        return new Favorite(user, null, place);
    }
}
