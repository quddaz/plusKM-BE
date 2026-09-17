package personal_projects.backend.domain.guardian.sms;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class NaverSensSmsSender implements EmergencySmsSender {

    private final NaverSensProperties properties;
    private final NaverApiSigner signer;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final Clock clock;

    public NaverSensSmsSender(
        NaverSensProperties properties,
        NaverApiSigner signer,
        ObjectMapper objectMapper
    ) {
        this(properties, signer, objectMapper, HttpClient.newHttpClient(), Clock.systemUTC());
    }

    NaverSensSmsSender(
        NaverSensProperties properties,
        NaverApiSigner signer,
        ObjectMapper objectMapper,
        HttpClient httpClient,
        Clock clock
    ) {
        this.properties = properties;
        this.signer = signer;
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
        this.clock = clock;
    }

    @Override
    public SmsSendResult send(String phoneNumber, String content) {
        validateConfiguration();
        String uri = "/sms/v2/services/%s/messages".formatted(properties.serviceId());
        String timestamp = String.valueOf(clock.millis());
        try {
            byte[] body = objectMapper.writeValueAsBytes(Map.of(
                "type", "LMS",
                "contentType", "COMM",
                "countryCode", "82",
                "from", digits(properties.from()),
                "subject", "plusKM 긴급 알림",
                "content", content,
                "messages", List.of(Map.of("to", digits(phoneNumber)))
            ));
            HttpRequest request = HttpRequest.newBuilder(URI.create(properties.endpoint() + uri))
                .header("Content-Type", "application/json; charset=utf-8")
                .header("x-ncp-apigw-timestamp", timestamp)
                .header("x-ncp-iam-access-key", properties.accessKey())
                .header("x-ncp-apigw-signature-v2", signer.sign(
                    "POST", uri, timestamp, properties.accessKey(), properties.secretKey()
                ))
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();
            HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("네이버 SENS 문자 발송에 실패했습니다. 상태 코드: " + response.statusCode());
            }
            return objectMapper.readValue(response.body(), SmsSendResult.class);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("네이버 SENS 문자 발송이 중단되었습니다.", exception);
        } catch (Exception exception) {
            if (exception instanceof IllegalStateException illegalStateException) {
                throw illegalStateException;
            }
            throw new IllegalStateException("네이버 SENS 문자 발송에 실패했습니다.", exception);
        }
    }

    private void validateConfiguration() {
        if (!properties.enabled()) {
            throw new IllegalStateException("네이버 SENS 문자 발송이 비활성화되어 있습니다.");
        }
        if (properties.serviceId().isBlank() || properties.accessKey().isBlank()
            || properties.secretKey().isBlank() || properties.from().isBlank()) {
            throw new IllegalStateException("네이버 SENS 설정이 완료되지 않았습니다.");
        }
    }

    private String digits(String phoneNumber) {
        return phoneNumber.replaceAll("[^0-9]", "");
    }
}
