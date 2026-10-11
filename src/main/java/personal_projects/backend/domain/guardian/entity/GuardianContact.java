package personal_projects.backend.domain.guardian.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import personal_projects.backend.common.entity.BaseAuditEntity;
import personal_projects.backend.domain.user.entity.User;

@Entity
@Getter
@Table(name = "guardian_contact")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GuardianContact extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 40)
    private String name;

    @Column(nullable = false, length = 20)
    private String relationship;

    @Column(name = "encrypted_phone_number", nullable = false, length = 512)
    private String encryptedPhoneNumber;

    @Column(nullable = false)
    private boolean active;

    private GuardianContact(User user, String name, String relationship, String encryptedPhoneNumber) {
        this.user = user;
        this.name = name;
        this.relationship = relationship;
        this.encryptedPhoneNumber = encryptedPhoneNumber;
        this.active = true;
    }

    public static GuardianContact create(
        User user,
        String name,
        String relationship,
        String encryptedPhoneNumber
    ) {
        return new GuardianContact(user, name, relationship, encryptedPhoneNumber);
    }

    public void update(String name, String relationship, String encryptedPhoneNumber, boolean active) {
        this.name = name;
        this.relationship = relationship;
        this.encryptedPhoneNumber = encryptedPhoneNumber;
        this.active = active;
    }
}
