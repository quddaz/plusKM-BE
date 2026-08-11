package personal_projects.backend.domain.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import personal_projects.backend.common.response.ErrorResponse;
import personal_projects.backend.common.exception.DomainErrorCode;

import java.io.IOException;

/**
 * AccessToken 은 있으나 권한이 맞지 않는 경우 - 403
 */
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(
            response.getWriter(),
            ErrorResponse.of(
                DomainErrorCode.ACCESS_DENIED,
                messageSource.getMessage(
                    DomainErrorCode.ACCESS_DENIED.messageKey(),
                    null,
                    request.getLocale()
                )
            )
        );
    }
}
