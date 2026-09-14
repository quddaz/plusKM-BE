package personal_projects.backend.common.exception.handler;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import personal_projects.backend.common.response.ErrorResponse;
import personal_projects.backend.common.exception.BusinessException;
import personal_projects.backend.common.exception.DomainErrorCode;
import personal_projects.backend.common.exception.code.CommonErrorCode;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final MessageSource messageSource;
    private final DomainErrorHttpMapper domainErrorHttpMapper;

    @ExceptionHandler(BusinessException.class)
    public ErrorResponse handleBusinessException(
        BusinessException exception,
        HttpServletResponse response
    ) {
        response.setStatus(domainErrorHttpMapper.statusOf(exception.code()).value());
        return toErrorResponse(exception);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException exception) {
        return toErrorResponse(CommonErrorCode.INVALID_INPUT);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(Exception exception) {
        log.error("Unhandled exception", exception);
        return toErrorResponse(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationExceptions(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ErrorResponse.of(
            CommonErrorCode.VALIDATION_ERROR,
            messageOf(CommonErrorCode.VALIDATION_ERROR),
            errors
        );
    }

    private ErrorResponse toErrorResponse(BusinessException exception) {
        return toErrorResponse(exception.code());
    }

    private ErrorResponse toErrorResponse(DomainErrorCode code) {
        return ErrorResponse.of(code, messageOf(code));
    }

    private String messageOf(DomainErrorCode code) {
        return messageSource.getMessage(
            code.messageKey(),
            null,
            LocaleContextHolder.getLocale()
        );
    }
}
