package personal_projects.backend.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import personal_projects.backend.domain.auth.token.JwtProperties;
import personal_projects.backend.domain.emergency.client.EmergencyApiProperties;
import personal_projects.backend.domain.guardian.config.GuardianSecurityProperties;
import personal_projects.backend.domain.guardian.sms.NaverSensProperties;

@Configuration
@EnableConfigurationProperties({
    JwtProperties.class,
    EmergencyApiProperties.class,
    GuardianSecurityProperties.class,
    NaverSensProperties.class
})
public class ConfigurationPropertiesConfig {
}
