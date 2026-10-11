package personal_projects.backend.domain.guardian.crypto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.security.SecureRandom;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class GuardianPhoneCipherTest {

    private static final String KEY = Base64.getEncoder().encodeToString(new byte[32]);

    @Test
    void 전화번호를_AES_GCM으로_암호화하고_복호화한다() {
        GuardianPhoneCipher cipher = new GuardianPhoneCipher(KEY, new SecureRandom());

        String first = cipher.encrypt("01012345678");
        String second = cipher.encrypt("01012345678");

        assertThat(first).isNotEqualTo("01012345678").isNotEqualTo(second);
        assertThat(cipher.decrypt(first)).isEqualTo("01012345678");
        assertThat(cipher.decrypt(second)).isEqualTo("01012345678");
    }

    @Test
    void 잘못된_암호화키는_사용할_수_없다() {
        GuardianPhoneCipher cipher = new GuardianPhoneCipher("invalid", new SecureRandom());

        assertThatThrownBy(() -> cipher.encrypt("01012345678"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("32바이트");
    }
}
