package personal_projects.backend.domain.place.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "place",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_place_name_address",
        columnNames = {"name", "address"}
    )
)
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlaceCategory category;

    @Column(name = "facility_type", nullable = false, length = 40)
    private String facilityType;

    @Column(nullable = false)
    private String address;

    @Column(name = "tel", length = 40)
    private String phoneNumber;

    @Column(nullable = false)
    private boolean active;

    @Column(columnDefinition = "POINT SRID 4326", nullable = false)
    private Point coordinate;
}
