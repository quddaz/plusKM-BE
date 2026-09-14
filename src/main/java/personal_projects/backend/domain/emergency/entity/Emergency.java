package personal_projects.backend.domain.emergency.entity;

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
@Table(name = "emergency",
    indexes = {
        @Index(name = "idx_emergency_coordinate", columnList = "coordinate")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_emergency_name_address", columnNames = {"name", "address"})
    }
)
public class Emergency {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 20)
    private String hpid;

    private String name;

    private String address;

    @Column(name = "tel")
    private String phoneNumber;

    private boolean active;

    @Column(columnDefinition = "POINT")
    private Point coordinate;

    public static Emergency create(String hpid, String name, String address, String phoneNumber, Point coordinate) {
        return Emergency.builder().hpid(hpid).name(name).address(address).phoneNumber(phoneNumber)
            .active(true).coordinate(coordinate).build();
    }

    public void update(String name, String address, String phoneNumber, Point coordinate) {
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.coordinate = coordinate;
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

}
