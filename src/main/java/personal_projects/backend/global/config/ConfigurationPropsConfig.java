package personal_projects.backend.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import personal_projects.backend.oauth.util.jwt.JwtProperties;

@Configuration
@EnableConfigurationProperties(value = JwtProperties.class)
public class ConfigurationPropsConfig {
}
