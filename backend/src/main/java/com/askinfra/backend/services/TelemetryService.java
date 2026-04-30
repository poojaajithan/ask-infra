package com.askinfra.backend.services;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class TelemetryService {

    private final JdbcTemplate jdbcTemplate;
    private final ChatClient chatClient;

    private static final String TELEMETRY_SCHEMA = """
                    You are generating SQL for an H2 database.

                    Database schema:

                    Table: servers
                    - id (BIGINT, primary key)
                    - name (VARCHAR)
                    - type (VARCHAR)
                    - region (VARCHAR)

                    Table: metrics
                    - id (BIGINT, primary key)
                    - server_id (BIGINT, foreign key to servers.id)
                    - recorded_at (TIMESTAMP)
                    - cpu_percent (DOUBLE)
                    - memory_percent (DOUBLE)
                    - disk_percent (DOUBLE)

                    Relationship:
                    - metrics.server_id = servers.id

                    Rules:
                    - Only generate SELECT queries
                    - Do not generate INSERT, UPDATE, DELETE, DROP, ALTER, or TRUNCATE
                    - Return plain SQL only
                    - Do not use markdown
                    - Do not explain the query
            """;

    public TelemetryService(JdbcTemplate jdbcTemplate, ChatClient chatClient) {
        this.jdbcTemplate = jdbcTemplate;
        this.chatClient = chatClient;
    }

    private String generateSql(String message) {
        return chatClient
                .prompt()
                .system(TELEMETRY_SCHEMA)
                .user("""
                        Convert the following infrastructure question into one safe SQL SELECT query.

                        Question:
                        %s
                        """.formatted(message))
                .call()
                .content();
    }

    private void validateSql(String sql) {
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("Generated SQL must not be blank.");
        }

        String normalizeSql = sql.trim().toLowerCase();
        if (!normalizeSql.startsWith("select")) {
            throw new IllegalArgumentException("Only SELECT queries are allowed.");
        }

        if (normalizeSql.contains(";")) {
            throw new IllegalArgumentException("Multiple statements are not allowed.");
        }

        if (normalizeSql.contains("insert")
                || normalizeSql.contains("update")
                || normalizeSql.contains("delete")
                || normalizeSql.contains("drop")
                || normalizeSql.contains("alter")
                || normalizeSql.contains("truncate")) {
            throw new IllegalArgumentException("Unsafe SQL generated - Only SELECT queries are allowed.");
        }
    }

    private List<Map<String,Object>> executeSql(String sql) {
        return jdbcTemplate.queryForList(sql);
    }

    private String summarizeResult(String question, List<Map<String, Object>> rows) {
    return chatClient
            .prompt()
            .system("""
                    You are Ask Infra, an infrastructure monitoring assistant.

                    Your job is to explain SQL query results in clear, practical language.
                    Keep the answer concise and useful for an operator.
                    If the result is empty, clearly say no matching telemetry data was found.
                    """)
            .user("""
                    User question:
                    %s

                    SQL result rows:
                    %s
                    """.formatted(question, rows))
            .call()
            .content();
    }

    public String answerTelemetryQuestion(String message){
        String sql = generateSql(message);
        validateSql(sql);
        List<Map<String, Object>> rows = executeSql(sql);
        return summarizeResult(message, rows);
    }
}
