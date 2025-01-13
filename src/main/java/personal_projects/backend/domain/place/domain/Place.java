package personal_projects.backend.domain.place.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;


@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Place {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private Place_type place_type;

    private String address;

    private String tel;

    @Column(nullable = false, columnDefinition = "POINT SRID 4326")
    private Point coordinate;

    @Builder
    public Place(String name, String place_type, String address, String tel, Point coordinate) {
        this.name = name;
        this.place_type = Place_type.valueOf(place_type);
        this.address = address;
        this.tel = tel;
        this.coordinate = coordinate;
    }
}
