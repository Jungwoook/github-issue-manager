package com.jw.github_issue_manager.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(properties = {
    "app.github.pat-encryption-key=test-pat-key"
})
class PlatformSchemaIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void platformNeutralSchemaNamesAreCreated() {
        assertThat(tableColumns("PLATFORM_CONNECTIONS")).contains("EXTERNAL_USER_ID", "ACCOUNT_LOGIN", "BASE_URL");
        assertThat(tableColumns("REPOSITORY_CACHES")).contains("EXTERNAL_ID", "OWNER_KEY", "WEB_URL");
        assertThat(tableColumns("ISSUE_CACHES")).contains("EXTERNAL_ID", "REPOSITORY_EXTERNAL_ID", "NUMBER_OR_KEY");
        assertThat(tableColumns("COMMENT_CACHES")).contains("EXTERNAL_ID", "ISSUE_EXTERNAL_ID");
    }

    private List<String> tableColumns(String tableName) {
        return jdbcTemplate.queryForList(
            """
                select column_name
                from information_schema.columns
                where table_name = ?
                order by ordinal_position
                """,
            String.class,
            tableName
        );
    }
}
