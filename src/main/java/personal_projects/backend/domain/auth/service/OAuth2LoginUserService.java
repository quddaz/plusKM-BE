package personal_projects.backend.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import personal_projects.backend.domain.auth.dto.AuthenticatedUserPrincipal;
import personal_projects.backend.domain.auth.dto.social.GoogleOAuth2UserInfo;
import personal_projects.backend.domain.auth.dto.social.OAuth2UserInfo;
import personal_projects.backend.domain.auth.exception.UnsupportedOAuthProviderException;
import personal_projects.backend.domain.auth.exception.code.AuthErrorCode;
import personal_projects.backend.domain.user.entity.User;
import personal_projects.backend.domain.user.repository.UserRepository;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuth2LoginUserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Override
    public AuthenticatedUserPrincipal loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauthUser = super.loadUser(userRequest);

        OAuth2UserInfo userInfo = resolveUserInfo(userRequest.getClientRegistration().getRegistrationId(),
            oauthUser.getAttributes());

        User user = findOrCreateUser(userInfo);

        return AuthenticatedUserPrincipal.fromUser(user);
    }
    private OAuth2UserInfo resolveUserInfo(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case "google" -> new GoogleOAuth2UserInfo(attributes);
            default -> throw new UnsupportedOAuthProviderException(AuthErrorCode.UNSUPPORTED_OAUTH_PROVIDER);
        };
    }

    private User findOrCreateUser(OAuth2UserInfo userInfo) {
        return userRepository.findBySocialId(userInfo.providerId())
            .orElseGet(() -> userRepository.save(User.fromOAuth2UserInfo(userInfo)));
    }
}
