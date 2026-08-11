package personal_projects.backend.common.exception;

import java.util.Locale;

public interface DomainErrorCode {

    String name();

    default String messageKey() {
        return "error." + name().toLowerCase(Locale.ROOT);
    }

    default String titleKey() {
        return "error.title." + name().toLowerCase(Locale.ROOT);
    }
}
