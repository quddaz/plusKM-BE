package personal_projects.backend.global.admin;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "csv")
public record CsvProperties(
    String hospitalPath,
    String pharmacyPath,
    int batchSize
) {

}
