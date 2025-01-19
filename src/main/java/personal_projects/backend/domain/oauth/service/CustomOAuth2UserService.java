package personal_projects.backend.domain.oauth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import personal_projects.backend.domain.oauth.dto.CustomOAuth2User;
import personal_projects.backend.domain.oauth.dto.social.GoogleResponse;
import personal_projects.backend.domain.oauth.dto.social.Oauth2Response;
import personal_projects.backend.domain.oauth.exception.LoginTypeNotSupportException;
import personal_projects.backend.domain.oauth.exception.errorcode.AuthErrorCode;
import personal_projects.backend.domain.user.domain.User;
import personal_projects.backend.domain.user.repository.UserRepository;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Override
    public CustomOAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest); // OAuth2 사용자 정보를 가져옴

        // OAuth2Response를 제공업체에 따라 추출
        Oauth2Response oAuth2Response = getOAuth2Response(userRequest.getClientRegistration().getRegistrationId(),
            oAuth2User.getAttributes());

        // 사용자 정보 로드 또는 생성
        User user = getOrGenerateMember(oAuth2Response);

        // AuthUser 생성 및 반환
        return CustomOAuth2User.fromUser(user);
    }
    private Oauth2Response getOAuth2Response(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case "google" -> new GoogleResponse(attributes);
            default -> throw new LoginTypeNotSupportException(AuthErrorCode.LOGIN_TYPE_NOT_SUPPORT);
        };
    }

    private User getOrGenerateMember(Oauth2Response oauth2Response) {
        return userRepository.findBySocialId(oauth2Response.getProviderId())
            .orElseGet(() -> userRepository.save(User.fromOAuth2Response(oauth2Response)));
    }
}
