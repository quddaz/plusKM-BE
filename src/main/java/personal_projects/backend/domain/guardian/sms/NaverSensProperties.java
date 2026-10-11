package personal_projects.backend.domain.guardian.sms;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("guardian.sms")
public record NaverSensProperties(
    @DefaultValue("false") boolean enabled,
    @DefaultValue("https://sens.apigw.ntruss.com") String endpoint,
    @DefaultValue("") String serviceId,
    @DefaultValue("") String accessKey,
    @DefaultValue("") String secretKey,
    @DefaultValue("") String from
) {
}
