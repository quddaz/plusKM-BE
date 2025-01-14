package personal_projects.backend.domain.user.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 40)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_type", nullable = false, length = 20)
    private Oauth_type oauth_type;

    @Column(name = "social_id", nullable = false, length = 100)
    private String social_id;

    @Column(name = "region", nullable = false, length = 20)
    private String region;

    @Column(name = "email", nullable = false, length = 50)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Builder
    public User(String name, Oauth_type oauth_type, String social_id, String region, String email, Role role) {
        this.name = name;
        this.oauth_type = oauth_type;
        this.social_id = social_id;
        this.region = region;
        this.email = email;
        this.role = role;
    }
    public List<String> getRoles() {
        return List.of(this.role.name());
    }
}
