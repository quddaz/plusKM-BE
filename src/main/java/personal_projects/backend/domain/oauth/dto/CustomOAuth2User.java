package personal_projects.backend.domain.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import personal_projects.backend.domain.user.domain.User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Builder
@Getter
@AllArgsConstructor
public class CustomOAuth2User implements OAuth2User {
    private final Long userId;
    private final String socialId;
    private final String name;
    private final String email;
    private final List<String> roles;

    public static CustomOAuth2User fromUser(User user) {
        return CustomOAuth2User.builder()
            .userId(user.getId())
            .socialId(user.getSocialId())
            .name(user.getName())
            .email(user.getEmail())
            .roles(List.of(user.getRole().name()))
            .build();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }


}