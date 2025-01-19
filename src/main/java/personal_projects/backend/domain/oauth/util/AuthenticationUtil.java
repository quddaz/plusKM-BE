package personal_projects.backend.domain.oauth.util;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import personal_projects.backend.domain.user.domain.User;
import personal_projects.backend.domain.oauth.dto.CustomOAuth2User;

import java.util.ArrayList;
import java.util.List;

@Component
public class AuthenticationUtil {

    public static void makeAuthentication(User user) {
        // Authentication 정보 만들기
        CustomOAuth2User authUser = CustomOAuth2User.fromUser(user);

        // ContextHolder 에 Authentication 정보 저장
        Authentication auth = AuthenticationUtil.getAuthentication(authUser);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private static Authentication getAuthentication(CustomOAuth2User authUser) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>(authUser.getAuthorities());

        return new UsernamePasswordAuthenticationToken(authUser, "", grantedAuthorities);
    }
}