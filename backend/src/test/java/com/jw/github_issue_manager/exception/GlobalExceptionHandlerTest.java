package com.jw.github_issue_manager.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.jw.github_issue_manager.github.GitHubApiException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void githubApiExceptionsUsePlatformApiErrorContract() {
        var response = handler.handlePlatformApi(new GitHubApiException("platform upstream failure"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("PLATFORM_API_ERROR");
        assertThat(response.getBody().message()).isEqualTo("platform upstream failure");
    }

    @Test
    void restClient4xxPreservesStatusAndUsesPlatformApiErrorContract() {
        var exception = HttpClientErrorException.create(
            HttpStatus.NOT_FOUND,
            "Not Found",
            org.springframework.http.HttpHeaders.EMPTY,
            "missing remote resource".getBytes(),
            java.nio.charset.StandardCharsets.UTF_8
        );

        var response = handler.handleGitHubHttp(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("PLATFORM_API_ERROR");
        assertThat(response.getBody().message()).isEqualTo("missing remote resource");
    }

    @Test
    void restClient5xxIsConvertedToBadGateway() {
        var exception = HttpServerErrorException.create(
            HttpStatus.BAD_GATEWAY,
            "Bad Gateway",
            org.springframework.http.HttpHeaders.EMPTY,
            "upstream gateway error".getBytes(),
            java.nio.charset.StandardCharsets.UTF_8
        );

        var response = handler.handleGitHubHttp(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("PLATFORM_API_ERROR");
        assertThat(response.getBody().message()).isEqualTo("upstream gateway error");
    }
}
