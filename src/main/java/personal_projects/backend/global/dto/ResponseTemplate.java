package personal_projects.backend.global.dto;

import lombok.Builder;

import java.util.Collections;

@Builder
public record ResponseTemplate<T>(
    Boolean isSuccess,
    String code,
    String message,
    T results
) {
    public static final ResponseTemplate<Object> EMPTY_RESPONSE = ResponseTemplate.builder()
        .isSuccess(true)
        .code("REQUEST_OK")
        .message("요청이 승인되었습니다.")
        .results(Collections.EMPTY_MAP)
        .build();
    public static final ResponseTemplate<Object> JSON_AUTH_ERROR = ResponseTemplate.builder()
        .isSuccess(false)
        .code("JSON_AUTH_ERROR")
        .message("로그인 후 다시 접근해주시기 바랍니다.")
        .results(Collections.EMPTY_MAP)
        .build();

    public static final ResponseTemplate<Object> JSON_ROLE_ERROR = ResponseTemplate.builder()
        .isSuccess(false)
        .code("JSON_ROLE_ERROR")
        .message("가진 권한으로는 실행할 수 없는 기능입니다.")
        .results(Collections.EMPTY_MAP)
        .build();

    public static <T> ResponseTemplate<Object> from(T dto) {
        return ResponseTemplate.builder()
            .isSuccess(true)
            .code("REQUEST_OK")
            .message("request succeeded")
            .results(dto)
            .build();
    }
    public static <T> ResponseTemplate<Object> fail(T dto) {
        return ResponseTemplate.builder()
            .isSuccess(false)
            .code("Error")
            .message(dto.toString())
            .results(null)
            .build();
    }
}