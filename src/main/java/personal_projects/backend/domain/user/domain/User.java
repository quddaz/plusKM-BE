package personal_projects.backend.domain.user.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import personal_projects.backend.domain.oauth.dto.social.Oauth2Response;

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

    @Column(name = "socialId", nullable = false, length = 100)
    private String socialId;

    @Column(name = "email", nullable = false, length = 50)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Builder
    public User(String name, Oauth_type oauth_type, String socialId, String email, Role role) {
        this.name = name;
        this.oauth_type = oauth_type;
        this.socialId = socialId;
        this.email = email;
        this.role = role;
    }
    public static User fromOAuth2Response(Oauth2Response oAuth2Response) {
        return User.builder()
            .email(oAuth2Response.getEmail())
            .name(oAuth2Response.getName())
            .oauth_type(oAuth2Response.getProvider())
            .socialId(oAuth2Response.getProviderId())
            .role(Role.USER)
            .build();
    }
    public List<String> getRoles() {
        return List.of(this.role.name());
    }
}
