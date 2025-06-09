package personal_projects.backend.domain.place.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;
import personal_projects.backend.domain.bookmark.domain.BookMark;
import personal_projects.backend.domain.medical.domain.Medical;
import personal_projects.backend.domain.place.domain.enumType.Place_type;

import java.util.List;


@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "place", indexes = {
    @Index(name = "idx_place_coordinate", columnList = "coordinate")
})
public class Place {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private Place_type place_type;

    private String address;

    private String tel;

    @Column(columnDefinition = "POINT")
    private Point coordinate;

    @OneToMany(mappedBy = "place", orphanRemoval = true, cascade = CascadeType.REMOVE)
    private List<BookMark> bookMarks;

    @OneToMany(mappedBy = "place", orphanRemoval = true, cascade = CascadeType.REMOVE)
    private List<Medical> medicals;

    @Builder
    public Place(String name, String place_type, String address, String tel, Point coordinate) {
        this.name = name;
        this.place_type = Place_type.valueOf(place_type);
        this.address = address;
        this.tel = tel;
        this.coordinate = coordinate;
    }
}
