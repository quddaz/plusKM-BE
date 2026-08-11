package personal_projects.backend.domain.auth.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import personal_projects.backend.domain.user.entity.User;
import personal_projects.backend.domain.auth.dto.AuthenticatedUserPrincipal;

import java.util.ArrayList;
import java.util.List;

@Component
public class SecurityContextManager {

    public static void setAuthentication(User user) {
        AuthenticatedUserPrincipal principal = AuthenticatedUserPrincipal.fromUser(user);
        Authentication authentication = createAuthentication(principal);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private static Authentication createAuthentication(AuthenticatedUserPrincipal principal) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>(principal.getAuthorities());

        return new UsernamePasswordAuthenticationToken(principal, "", grantedAuthorities);
    }
}
