package personal_projects.backend.domain.emergency.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class EmergencyApiResponseParserTest {
    private final EmergencyApiResponseParser parser = new EmergencyApiResponseParser();

    @Test
    void 공공API_시설응답을_변환한다() {
        byte[] response = """
            <response><body><items><item>
              <hpid>A110001</hpid><dutyName>서울응급실</dutyName><dutyAddr>서울</dutyAddr>
              <dutyTel1>02-1111</dutyTel1><dutyTel3>02-2222</dutyTel3>
              <wgs84Lon>127.1</wgs84Lon><wgs84Lat>37.5</wgs84Lat>
            </item></items></body></response>
            """.getBytes(StandardCharsets.UTF_8);

        List<EmergencyFacilityData> facilities = parser.parse(response);

        assertThat(facilities).singleElement().satisfies(facility -> {
            assertThat(facility.hpid()).isEqualTo("A110001");
            assertThat(facility.phoneNumber()).isEqualTo("02-2222");
            assertThat(facility.longitude()).isEqualTo(127.1);
        });
    }

    @Test
    void 필수값이_없으면_거부한다() {
        byte[] response = "<response><item><hpid>A1</hpid></item></response>"
            .getBytes(StandardCharsets.UTF_8);

        assertThatThrownBy(() -> parser.parse(response)).isInstanceOf(IllegalStateException.class);
    }
}
