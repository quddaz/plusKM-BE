package personal_projects.backend.domain.place.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;
import personal_projects.backend.domain.bookmark.entity.Bookmark;
import personal_projects.backend.domain.medicalrecord.entity.MedicalRecord;
import personal_projects.backend.domain.place.type.PlaceType;

import java.util.List;


@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "place",
    indexes = {
        @Index(name = "idx_place_coordinate", columnList = "coordinate")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_place_name_address", columnNames = {"name", "address"})
    }
)
public class Place {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "place_type")
    private PlaceType placeType;

    private String address;

    @Column(name = "tel")
    private String phoneNumber;

    private boolean active;

    @Column(columnDefinition = "POINT")
    private Point coordinate;

    @OneToMany(mappedBy = "place", orphanRemoval = true, cascade = CascadeType.REMOVE)
    private List<Bookmark> bookmarks;

    @OneToMany(mappedBy = "place", orphanRemoval = true, cascade = CascadeType.REMOVE)
    private List<MedicalRecord> medicalRecords;
}
