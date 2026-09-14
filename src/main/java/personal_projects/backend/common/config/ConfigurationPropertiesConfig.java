package personal_projects.backend.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import personal_projects.backend.domain.auth.token.JwtProperties;
import personal_projects.backend.domain.emergency.client.EmergencyApiProperties;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, EmergencyApiProperties.class})
public class ConfigurationPropertiesConfig {
}
