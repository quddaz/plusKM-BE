package personal_projects.backend.domain.auth.dto.social;

import personal_projects.backend.domain.user.type.OAuthProvider;

import java.util.Map;

public record GoogleOAuth2UserInfo(Map<String, Object> attributes) implements OAuth2UserInfo {

    @Override
    public OAuthProvider provider() {
        return OAuthProvider.GOOGLE;
    }

    @Override
    public String providerId() {
        return attributes.get("sub").toString();
    }

    @Override
    public String email() {
        return attributes.get("email").toString();
    }

    @Override
    public String name() {
        return attributes.get("name").toString();
    }

    @Override
    public String profileImageUrl() {
        return attributes.get("picture").toString();
    }
}
