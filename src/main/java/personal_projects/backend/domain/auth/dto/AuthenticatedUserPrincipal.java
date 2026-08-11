package personal_projects.backend.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import personal_projects.backend.domain.user.entity.User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Builder
@Getter
@AllArgsConstructor
public class AuthenticatedUserPrincipal implements OAuth2User {
    private final Long userId;
    private final String socialId;
    private final String name;
    private final String email;
    private final List<String> roles;

    public static AuthenticatedUserPrincipal fromUser(User user) {
        return AuthenticatedUserPrincipal.builder()
            .userId(user.getId())
            .socialId(user.getSocialId())
            .name(user.getName())
            .email(user.getEmail())
            .roles(user.roleAuthorities())
            .build();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Collections.emptyMap();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
            .map(SimpleGrantedAuthority::new)
            .toList();
    }


}
