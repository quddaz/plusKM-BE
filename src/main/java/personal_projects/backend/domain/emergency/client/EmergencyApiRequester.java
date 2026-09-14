package personal_projects.backend.domain.emergency.client;

import java.io.IOException;
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

    private static final String FACILITY_PATH = "/getEgytListInfoInqire";
    private static final int SUCCESS_STATUS_CODE = 200;

    private final EmergencyApiProperties properties;
    private final HttpClient httpClient;

    public EmergencyApiRequester(EmergencyApiProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder().connectTimeout(properties.timeout()).build();
    }

    public byte[] requestFacilities() {
        HttpRequest request = createRequest(createFacilityUri());
        HttpResponse<byte[]> response = send(request);
        validate(response);
        return response.body();
    }

    private URI createFacilityUri() {
        String query = "?serviceKey=%s&pageNo=1&numOfRows=1000".formatted(encodeServiceKey());
        return URI.create(properties.baseUrl() + FACILITY_PATH + query);
    }

    private String encodeServiceKey() {
        String encodedKey = properties.serviceKey().trim().replace("+", "%2B");
        String decodedKey = URLDecoder.decode(encodedKey, StandardCharsets.UTF_8);
        return URLEncoder.encode(decodedKey, StandardCharsets.UTF_8);
    }

    private HttpRequest createRequest(URI uri) {
        return HttpRequest.newBuilder(uri)
            .timeout(properties.timeout())
            .GET()
            .build();
    }

    private HttpResponse<byte[]> send(HttpRequest request) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("응급실 API 요청에 실패했습니다.");
        } catch (IOException exception) {
            throw new IllegalStateException("응급실 API 요청에 실패했습니다.");
        }
    }

    private void validate(HttpResponse<byte[]> response) {
        if (response.statusCode() != SUCCESS_STATUS_CODE) {
            throw new IllegalStateException("응급실 API 요청에 실패했습니다.");
        }
    }
}
