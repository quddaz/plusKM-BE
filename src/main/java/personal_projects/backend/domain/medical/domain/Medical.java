package personal_projects.backend.domain.medical.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import personal_projects.backend.domain.medical.domain.enumType.Medical_department;
import personal_projects.backend.domain.place.domain.Place;
import personal_projects.backend.domain.user.domain.User;
import personal_projects.backend.global.domain.BaseTimeEntity;
@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Medical extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    private String content;

    @Enumerated(EnumType.STRING)
    private Medical_department department;

    private Long medical_fee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Medical(String content, String department, Long medical_fee, Place place, User user) {
        this.content = content;
        this.department = Medical_department.valueOf(department);
        this.medical_fee = medical_fee;
        this.place = place;
        this.user = user;
    }


}
