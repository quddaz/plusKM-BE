package personal_projects.backend.domain.user.type;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Role {
    ADMIN("ROLE_ADMIN"),
    USER("ROLE_USER"),
    ;

    private final String authority;

    public String authority() {
        return authority;
    }
}
