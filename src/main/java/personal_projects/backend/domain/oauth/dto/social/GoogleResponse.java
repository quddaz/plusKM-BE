package personal_projects.backend.oauth.dto.social;

import personal_projects.backend.domain.user.domain.Oauth_type;

import java.util.Map;

public record GoogleResponse(Map<String, Object> attribute) implements Oauth2Response {

    @Override
    public Oauth_type getProvider() {
        return Oauth_type.GOOGLE;
    }

    @Override
    public String getProviderId() {
        return attribute.get("sub").toString();
    }

    @Override
    public String getEmail() {
        return attribute.get("email").toString();
    }

    @Override
    public String getName() {
        return attribute.get("name").toString();
    }

    @Override
    public String getProfileImage() {
        return attribute.get("picture").toString();
    }
}
