package personal_projects.backend.domain.place.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "csv")
public record PlaceCsvProperties(
    String hospitalPath,
    String pharmacyPath,
    int batchSize,
    boolean importOnStartup
) {
}
