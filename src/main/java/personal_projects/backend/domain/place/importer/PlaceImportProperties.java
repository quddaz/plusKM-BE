package personal_projects.backend.domain.place.importer;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "csv")
public record PlaceImportProperties(
    String hospitalPath,
    String pharmacyPath,
    int batchSize
) {

}
