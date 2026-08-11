package personal_projects.backend.global.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import personal_projects.backend.domain.bookmark.exception.BookMarkNotFoundException;
import personal_projects.backend.domain.medical.exception.MedicalNotFoundException;
import personal_projects.backend.domain.oauth.exception.LoginTypeNotSupportException;
import personal_projects.backend.domain.oauth.exception.TokenNotValidException;
import personal_projects.backend.domain.place.exception.PlaceNotFoundException;
import personal_projects.backend.domain.user.exception.UserNotFoundException;
import personal_projects.backend.global.admin.exception.AdminNotAuthorizedException;
import personal_projects.backend.global.admin.exception.FileConversionException;
import personal_projects.backend.global.dto.ErrorResponse;
import personal_projects.backend.global.exception.BusinessException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ExceptionManager {

    @ExceptionHandler({
        PlaceNotFoundException.class,
        MedicalNotFoundException.class,
        BookMarkNotFoundException.class,
        UserNotFoundException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(BusinessException exception) {
        return from(exception);
    }

    @ExceptionHandler({TokenNotValidException.class, LoginTypeNotSupportException.class})
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public ErrorResponse handleNotAcceptable(BusinessException exception) {
        return from(exception);
    }

    @ExceptionHandler(AdminNotAuthorizedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleForbidden(BusinessException exception) {
        return from(exception);
    }

    @ExceptionHandler(FileConversionException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleFileConversion(BusinessException exception) {
        log.error("File conversion failed", exception);
        return from(exception);
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

    private ErrorResponse from(BusinessException exception) {
        return ErrorResponse.of(
            exception.getErrorCode().toString(),
            exception.getErrorCode().getMessage()
        );
    }
}
