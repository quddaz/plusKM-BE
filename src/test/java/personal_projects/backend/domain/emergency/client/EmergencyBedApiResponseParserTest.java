package personal_projects.backend.domain.emergency.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class EmergencyBedApiResponseParserTest {

    private final EmergencyBedApiResponseParser parser = new EmergencyBedApiResponseParser();

    @Test
    void 공공API_병상응답을_변환한다() {
        byte[] response = """
            <response><body><items><item>
              <hpid>A110001</hpid><hvec>5</hvec><hvoc>2</hvoc>
              <hvicc>0</hvicc><hvgc>-1</hvgc><hvidate>20260917123000</hvidate>
            </item></items></body></response>
            """.getBytes(StandardCharsets.UTF_8);

        List<EmergencyBedData> beds = parser.parse(response);

        assertThat(beds).singleElement().satisfies(bed -> {
            assertThat(bed.hpid()).isEqualTo("A110001");
            assertThat(bed.emergencyRoom()).isEqualTo(5);
            assertThat(bed.operatingRoom()).isEqualTo(2);
            assertThat(bed.intensiveCareUnit()).isZero();
            assertThat(bed.inpatientRoom()).isNull();
            assertThat(bed.updatedAt()).isEqualTo("20260917123000");
        });
    }

    @Test
    void 값이_없는_병상수는_null로_변환한다() {
        byte[] response = """
            <response><body><items><item>
              <hpid>A110001</hpid><hvec></hvec>
            </item></items></body></response>
            """.getBytes(StandardCharsets.UTF_8);

        assertThat(parser.parse(response).getFirst().emergencyRoom()).isNull();
    }
}
