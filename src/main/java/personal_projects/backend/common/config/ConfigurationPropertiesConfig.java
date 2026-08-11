package personal_projects.backend.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import personal_projects.backend.domain.auth.token.JwtProperties;
import personal_projects.backend.domain.place.importer.PlaceImportProperties;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, PlaceImportProperties.class})
public class ConfigurationPropertiesConfig {
}
