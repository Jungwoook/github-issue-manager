package com.jw.github_issue_manager.connection.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.jw.github_issue_manager.exception.PlatformIntegrationException;

class PatCryptoServiceTest {

    private static final String RAW_KEY = "unit-test-pat-encryption-key";

    @Test
    void encryptAndDecryptRoundTrip() {
        PatCryptoService service = new PatCryptoService(RAW_KEY);

        String encrypted = service.encrypt("github_pat_round_trip");

        assertThat(encrypted).startsWith("v2:");
        assertThat(service.decrypt(encrypted)).isEqualTo("github_pat_round_trip");
    }

    @Test
    void encryptUsesRandomIvForEachToken() {
        PatCryptoService service = new PatCryptoService(RAW_KEY);

        String first = service.encrypt("github_pat_same_value");
        String second = service.encrypt("github_pat_same_value");

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void decryptRejectsUnsupportedFormat() {
        PatCryptoService service = new PatCryptoService(RAW_KEY);

        assertThatThrownBy(() -> service.decrypt("legacy-base64-value"))
            .isInstanceOf(PlatformIntegrationException.class)
            .hasMessage("Failed to process platform access token.");
    }

    @Test
    void decryptFailsWhenCipherTextIsTampered() {
        PatCryptoService service = new PatCryptoService(RAW_KEY);
        String encrypted = service.encrypt("github_pat_tamper");
        String[] parts = encrypted.split(":");
        String tampered = parts[0] + ":" + parts[1] + ":" + parts[2].substring(0, parts[2].length() - 2) + "AA";

        assertThatThrownBy(() -> service.decrypt(tampered))
            .isInstanceOf(PlatformIntegrationException.class)
            .hasMessage("Failed to process platform access token.");
    }
}
