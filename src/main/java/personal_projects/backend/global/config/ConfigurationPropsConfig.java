package personal_projects.backend.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import personal_projects.backend.domain.oauth.util.jwt.JwtProperties;
import personal_projects.backend.global.admin.CsvProperties;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, CsvProperties.class})
public class ConfigurationPropsConfig {
}
