package com.jw.github_issue_manager.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.jw.github_issue_manager.exception.PlatformIntegrationException;

@Service
public class PatCryptoService {

    private static final String ENCRYPTION_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String KEY_ALGORITHM = "AES";
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int GCM_IV_LENGTH_BYTES = 12;
    private static final String CIPHER_TEXT_VERSION = "v2";

    private final String patEncryptionKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public PatCryptoService(
        @Value("${app.security.pat-encryption-key:${app.github.pat-encryption-key:}}") String patEncryptionKey
    ) {
        this.patEncryptionKey = patEncryptionKey;
    }

    public String encrypt(String plainText) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ENCRYPTION_TRANSFORMATION);
            cipher.init(
                Cipher.ENCRYPT_MODE,
                new SecretKeySpec(secretKey(), KEY_ALGORITHM),
                new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            );

            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return CIPHER_TEXT_VERSION
                + ":"
                + Base64.getEncoder().encodeToString(iv)
                + ":"
                + Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception exception) {
            throw new PlatformIntegrationException("Failed to encrypt platform access token.", exception);
        }
    }

    public String decrypt(String encryptedText) {
        try {
            String[] parts = encryptedText.split(":");
            if (parts.length != 3 || !CIPHER_TEXT_VERSION.equals(parts[0])) {
                throw new PlatformIntegrationException("Unsupported platform access token cipher text format.");
            }

            byte[] iv = Base64.getDecoder().decode(parts[1]);
            byte[] encrypted = Base64.getDecoder().decode(parts[2]);

            Cipher cipher = Cipher.getInstance(ENCRYPTION_TRANSFORMATION);
            cipher.init(
                Cipher.DECRYPT_MODE,
                new SecretKeySpec(secretKey(), KEY_ALGORITHM),
                new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            );

            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            throw new PlatformIntegrationException("Failed to process platform access token.", exception);
        }
    }

    private byte[] secretKey() {
        String rawKey = patEncryptionKey;
        if (rawKey == null || rawKey.isBlank()) {
            throw new PlatformIntegrationException("Platform access token encryption key is not configured.");
        }

        try {
            return MessageDigest.getInstance("SHA-256").digest(rawKey.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new PlatformIntegrationException("Failed to initialize platform access token encryption key.", exception);
        }
    }
}
