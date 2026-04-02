package com.jw.github_issue_manager.controller;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.jayway.jsonpath.JsonPath;

final class JsonTestUtils {

    private JsonTestUtils() {
    }

    static long readLong(String json, String expression) {
        Number value = JsonPath.read(json, expression);
        return value.longValue();
    }

    static String queryParameter(String uri, String key) {
        String query = URI.create(uri).getQuery();
        return Arrays.stream(query.split("&"))
            .map(item -> item.split("=", 2))
            .filter(parts -> parts.length == 2 && parts[0].equals(key))
            .map(parts -> URLDecoder.decode(parts[1], StandardCharsets.UTF_8))
            .findFirst()
            .orElseThrow();
    }
}
