package personal_projects.backend.domain.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import personal_projects.backend.domain.auth.security.SecurityContextManager;
import personal_projects.backend.domain.user.entity.User;
import personal_projects.backend.domain.auth.token.JwtTokenProvider;

import java.io.IOException;
import java.util.NoSuchElementException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    public static final String AUTHORIZATION_HEADER = "Authorization";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String token = resolveToken(request);
        String requestUri = request.getRequestURI();

        if (token != null && jwtTokenProvider.validateToken(token)) {
            try {
                User user = jwtTokenProvider.resolveUser(token);
                SecurityContextManager.setAuthentication(user);
                log.debug("Authenticated user '{}' for URI '{}'", user.getId(), requestUri);
            } catch (NoSuchElementException exception) {
                // 서명은 유효해도 사용자가 삭제됐다면 인증되지 않은 요청으로 처리한다.
                org.springframework.security.core.context.SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
