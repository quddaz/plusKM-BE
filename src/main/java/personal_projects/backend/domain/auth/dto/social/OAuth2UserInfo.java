package personal_projects.backend.domain.auth.dto.social;

import personal_projects.backend.domain.user.type.OAuthProvider;

public interface OAuth2UserInfo {
    OAuthProvider provider();
    String providerId();
    String email();
    String name();
    String profileImageUrl();
}
