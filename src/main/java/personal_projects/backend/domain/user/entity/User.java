package personal_projects.backend.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import personal_projects.backend.domain.auth.dto.social.OAuth2UserInfo;
import personal_projects.backend.domain.user.type.OAuthProvider;
import personal_projects.backend.domain.user.type.Role;

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
    private OAuthProvider oauthProvider;

    @Column(name = "socialId", nullable = false, length = 100)
    private String socialId;

    @Column(name = "email", nullable = false, length = 50)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;


    public static User fromOAuth2UserInfo(OAuth2UserInfo userInfo) {
        return User.builder()
            .email(userInfo.email())
            .name(userInfo.name())
            .oauthProvider(userInfo.provider())
            .socialId(userInfo.providerId())
            .role(Role.USER)
            .build();
    }
    public List<String> roleAuthorities() {
        return List.of(role.authority());
    }
}
