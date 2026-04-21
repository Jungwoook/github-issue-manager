package com.jw.github_issue_manager.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class HealthControllerIntegrationTest {

    @Test
    void healthEndpointReturnsApplicationStatus() {
        HealthController controller = new HealthController();

        var response = controller.check();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo("UP");
        assertThat(response.getBody().application()).isEqualTo("github-issue-manager");
        assertThat(response.getBody().timestamp()).isNotNull();
    }
}
