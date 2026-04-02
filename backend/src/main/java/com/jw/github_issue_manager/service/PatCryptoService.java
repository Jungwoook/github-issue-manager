package com.jw.github_issue_manager.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

import com.jw.github_issue_manager.github.GitHubApiException;
import com.jw.github_issue_manager.github.GitHubIntegrationProperties;

@Service
public class PatCryptoService {

    private final GitHubIntegrationProperties properties;

    public PatCryptoService(GitHubIntegrationProperties properties) {
        this.properties = properties;
    }

    public String encrypt(String plainText) {
        return Base64.getEncoder().encodeToString(runCipher(Cipher.ENCRYPT_MODE, plainText.getBytes(StandardCharsets.UTF_8)));
    }

    public String decrypt(String encryptedText) {
        byte[] decoded = Base64.getDecoder().decode(encryptedText);
        return new String(runCipher(Cipher.DECRYPT_MODE, decoded), StandardCharsets.UTF_8);
    }

    private byte[] runCipher(int mode, byte[] input) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(mode, new SecretKeySpec(secretKey(), "AES"));
            return cipher.doFinal(input);
        } catch (Exception exception) {
            throw new GitHubApiException("Failed to process GitHub personal access token.", exception);
        }
    }

    private byte[] secretKey() {
        String rawKey = properties.patEncryptionKey();
        if (rawKey == null || rawKey.isBlank()) {
            throw new GitHubApiException("PAT encryption key is not configured.");
        }

        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawKey.getBytes(StandardCharsets.UTF_8));
            byte[] key = new byte[16];
            System.arraycopy(digest, 0, key, 0, key.length);
            return key;
        } catch (Exception exception) {
            throw new GitHubApiException("Failed to initialize PAT encryption key.", exception);
        }
    }
}
