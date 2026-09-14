package personal_projects.backend.common.exception;

public final class BusinessException extends RuntimeException {

    private final DomainErrorCode code;

    public BusinessException(DomainErrorCode code) {
        super(code.name());
        this.code = code;
    }

    public DomainErrorCode code() {
        return code;
    }
}
