package personal_projects.backend.domain.guardian.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("guardian")
public record GuardianSecurityProperties(
    @DefaultValue("") String encryptionKey
) {
}
