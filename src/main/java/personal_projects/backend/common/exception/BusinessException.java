package personal_projects.backend.common.exception;

public abstract class BusinessException extends RuntimeException {

    private final DomainErrorCode code;

    protected BusinessException(DomainErrorCode code) {
        super(code.name());
        this.code = code;
    }

    public DomainErrorCode code() {
        return code;
    }
}
