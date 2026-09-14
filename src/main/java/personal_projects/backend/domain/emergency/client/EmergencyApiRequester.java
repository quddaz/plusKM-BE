package personal_projects.backend.domain.emergency.client;

import java.net.URI;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;

@Component
public class EmergencyApiRequester {
    private final EmergencyApiProperties properties;
    private final HttpClient httpClient;

    public EmergencyApiRequester(EmergencyApiProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder().connectTimeout(properties.timeout()).build();
    }

    public byte[] requestFacilities() {
        try {
            String decodedKey = URLDecoder.decode(
                properties.serviceKey().trim().replace("+", "%2B"), StandardCharsets.UTF_8);
            String serviceKey = URLEncoder.encode(decodedKey, StandardCharsets.UTF_8);
            URI uri = URI.create(properties.baseUrl() + "/getEgytListInfoInqire?serviceKey=" + serviceKey
                + "&pageNo=1&numOfRows=1000");
            HttpRequest request = HttpRequest.newBuilder(uri).timeout(properties.timeout()).GET().build();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("응급실 API 요청에 실패했습니다.");
            }
            return response.body();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("응급실 API 요청에 실패했습니다.");
        } catch (Exception exception) {
            throw new IllegalStateException("응급실 API 요청에 실패했습니다.");
        }
    }
}
