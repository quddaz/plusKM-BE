package personal_projects.backend.domain.guardian.sms;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

@Component
public class NaverApiSigner {

    public String sign(
        String method,
        String uri,
        String timestamp,
        String accessKey,
        String secretKey
    ) {
        try {
            String message = method + " " + uri + "\n" + timestamp + "\n" + accessKey;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getEncoder().encodeToString(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("네이버 Cloud API 서명을 생성할 수 없습니다.", exception);
        }
    }
}
