package personal_projects.backend.domain.oauth.dto.social;

import personal_projects.backend.domain.user.domain.Oauth_type;

public interface Oauth2Response {
    //제공자 (Ex. naver, google, ...)
    Oauth_type getProvider();
    //제공자에서 발급해주는 아이디(번호)
    String getProviderId();
    //이메일
    String getEmail();
    //사용자 실명 (설정한 이름)
    String getName();
    String getProfileImage();
}
