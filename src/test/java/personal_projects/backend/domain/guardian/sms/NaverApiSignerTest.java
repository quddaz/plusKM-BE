package personal_projects.backend.domain.guardian.sms;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NaverApiSignerTest {

    private final NaverApiSigner signer = new NaverApiSigner();

    @Test
    void 네이버_Cloud_API_규격으로_서명한다() {
        String signature = signer.sign(
            "POST",
            "/sms/v2/services/test/messages",
            "1700000000000",
            "access-key",
            "secret-key"
        );

        assertThat(signature).isEqualTo("lBEtX4EVp1eZgt/ouqQ7gE+Fn6Dfk9++/63uJWFVGj8=");
    }
}
