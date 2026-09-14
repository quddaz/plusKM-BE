package personal_projects.backend.domain.emergency.client;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("emergency.api")
public record EmergencyApiProperties(
    @DefaultValue("https://apis.data.go.kr/B552657/ErmctInfoInqireService") String baseUrl,
    @DefaultValue("") String serviceKey,
    @DefaultValue("10s") Duration timeout
) {
}
