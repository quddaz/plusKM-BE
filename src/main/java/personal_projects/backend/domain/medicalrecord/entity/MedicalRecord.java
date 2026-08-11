package personal_projects.backend.domain.medicalrecord.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import personal_projects.backend.domain.medicalrecord.type.MedicalDepartment;
import personal_projects.backend.domain.place.entity.Place;
import personal_projects.backend.domain.user.entity.User;
import personal_projects.backend.common.entity.BaseAuditEntity;
@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "medical")
public class MedicalRecord extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content")
    private String description;

    @Enumerated(EnumType.STRING)
    private MedicalDepartment department;

    @Column(name = "medical_fee")
    private Long medicalFee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
