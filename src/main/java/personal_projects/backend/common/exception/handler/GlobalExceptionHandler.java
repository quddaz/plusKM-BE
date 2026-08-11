package personal_projects.backend.common.exception.handler;

import lombok.extern.slf4j.Slf4j;
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

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

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
        return ErrorResponse.of("INVALID_ARGUMENT", exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(Exception exception) {
        log.error("Unhandled exception", exception);
        return ErrorResponse.of("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");
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

        return ErrorResponse.of("VALIDATION_ERROR", errors.toString());
    }

    private ErrorResponse toErrorResponse(BusinessException exception) {
        return ErrorResponse.of(
            exception.getErrorCode().toString(),
            exception.getErrorCode().getMessage()
        );
    }
}
