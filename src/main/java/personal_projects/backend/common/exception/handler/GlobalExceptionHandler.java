package personal_projects.backend.common.exception.handler;

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
import personal_projects.backend.domain.bookmark.exception.BookmarkNotFoundException;
import personal_projects.backend.domain.medicalrecord.exception.MedicalRecordNotFoundException;
import personal_projects.backend.domain.auth.exception.UnsupportedOAuthProviderException;
import personal_projects.backend.domain.auth.exception.InvalidRefreshTokenException;
import personal_projects.backend.domain.place.exception.PlaceNotFoundException;
import personal_projects.backend.domain.user.exception.UserNotFoundException;
import personal_projects.backend.domain.place.importer.exception.PlaceImportForbiddenException;
import personal_projects.backend.domain.place.importer.exception.PlaceImportException;
import personal_projects.backend.common.response.ErrorResponse;
import personal_projects.backend.common.exception.BusinessException;
import personal_projects.backend.common.exception.DomainErrorCode;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler({
        PlaceNotFoundException.class,
        MedicalRecordNotFoundException.class,
        BookmarkNotFoundException.class,
        UserNotFoundException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(BusinessException exception) {
        return toErrorResponse(exception);
    }

    @ExceptionHandler({InvalidRefreshTokenException.class, UnsupportedOAuthProviderException.class})
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public ErrorResponse handleUnsupportedAuthRequest(BusinessException exception) {
        return toErrorResponse(exception);
    }

    @ExceptionHandler(PlaceImportForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handlePlaceImportForbidden(BusinessException exception) {
        return toErrorResponse(exception);
    }

    @ExceptionHandler(PlaceImportException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handlePlaceImportFailure(BusinessException exception) {
        log.error("Place import failed", exception);
        return toErrorResponse(exception);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException exception) {
        return toErrorResponse(DomainErrorCode.INVALID_INPUT);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(Exception exception) {
        log.error("Unhandled exception", exception);
        return toErrorResponse(DomainErrorCode.INTERNAL_SERVER_ERROR);
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
            DomainErrorCode.VALIDATION_ERROR,
            messageOf(DomainErrorCode.VALIDATION_ERROR),
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
