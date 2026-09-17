package personal_projects.backend.domain.guardian.crypto;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;
import personal_projects.backend.domain.guardian.config.GuardianSecurityProperties;

@Component
public class GuardianPhoneCipher {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final String encodedKey;
    private final SecureRandom secureRandom;

    public GuardianPhoneCipher(GuardianSecurityProperties properties) {
        this(properties.encryptionKey(), new SecureRandom());
    }

    GuardianPhoneCipher(String encodedKey, SecureRandom secureRandom) {
        this.encodedKey = encodedKey;
        this.secureRandom = secureRandom;
    }

    public String encrypt(String plainText) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);
            Cipher cipher = cipher(Cipher.ENCRYPT_MODE, iv);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(ByteBuffer.allocate(iv.length + encrypted.length).put(iv).put(encrypted).array());
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("보호자 전화번호를 암호화할 수 없습니다.", exception);
        }
    }

    public String decrypt(String encryptedText) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(encryptedText);
            if (decoded.length <= IV_LENGTH) {
                throw new IllegalArgumentException("암호화된 보호자 전화번호 형식이 올바르지 않습니다.");
            }
            byte[] iv = new byte[IV_LENGTH];
            byte[] encrypted = new byte[decoded.length - IV_LENGTH];
            System.arraycopy(decoded, 0, iv, 0, iv.length);
            System.arraycopy(decoded, iv.length, encrypted, 0, encrypted.length);
            return new String(cipher(Cipher.DECRYPT_MODE, iv).doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new IllegalStateException("보호자 전화번호를 복호화할 수 없습니다.", exception);
        }
    }

    private Cipher cipher(int mode, byte[] iv) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(
            mode,
            new SecretKeySpec(decodeKey(encodedKey), "AES"),
            new GCMParameterSpec(TAG_LENGTH_BITS, iv)
        );
        return cipher;
    }

    private static byte[] decodeKey(String encodedKey) {
        if (encodedKey == null || encodedKey.isBlank()) {
            throw new IllegalStateException("GUARDIAN_PHONE_ENCRYPTION_KEY가 설정되지 않았습니다.");
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(encodedKey);
            if (decoded.length != 32) {
                throw new IllegalStateException("보호자 전화번호 암호화 키는 32바이트여야 합니다.");
            }
            return decoded;
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("보호자 전화번호 암호화 키는 Base64 형식이어야 합니다.", exception);
        }
    }
}
