package com.learningpurpose.platformopsservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseSchemaOpsService {

    private final JdbcTemplate jdbcTemplate;

    private static final Pattern SAFE_DDL_PATTERN = Pattern.compile(
            "^(ALTER\\s+TABLE|CREATE\\s+INDEX|DROP\\s+INDEX|COMMENT\\s+ON)\\s+.*",
            Pattern.CASE_INSENSITIVE
    );

    public List<String> listTables() {
        String sql = """
            SELECT table_name 
            FROM information_schema.tables 
            WHERE table_schema = 'public' AND table_type = 'BASE TABLE' 
            ORDER BY table_name;
        """;
        return jdbcTemplate.queryForList(sql, String.class);
    }

    public List<ColumnDefinition> getTableSchema(String tableName) {
        String sql = """
            SELECT column_name, data_type, is_nullable, column_default, character_maximum_length
            FROM information_schema.columns 
            WHERE table_schema = 'public' AND table_name = ?
            ORDER BY ordinal_position;
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new ColumnDefinition(
                rs.getString("column_name"),
                rs.getString("data_type"),
                "YES".equalsIgnoreCase(rs.getString("is_nullable")),
                rs.getString("column_default"),
                rs.getObject("character_maximum_length") != null ? rs.getInt("character_maximum_length") : null
        ), tableName);
    }

    @Transactional
    public DdlResult executeControlledDdl(String ddlQuery) {
        String sanitized = ddlQuery.trim().replaceAll(";+$", "");

        // Enforce safety boundary: prevent accidental DROP DATABASE / DROP TABLE executions
        if (!SAFE_DDL_PATTERN.matcher(sanitized).matches()) {
            throw new IllegalArgumentException("Operation rejected. Only ALTER TABLE, CREATE/DROP INDEX, and COMMENT statements are permitted.");
        }

        long start = System.currentTimeMillis();
        jdbcTemplate.execute(sanitized);
        long elapsed = System.currentTimeMillis() - start;

        log.info("Executed administrative DDL: [{}] in {}ms", sanitized, elapsed);
        return new DdlResult(true, "DDL executed successfully", elapsed);
    }

    public record ColumnDefinition(String columnName, String dataType, boolean nullable, String defaultValue, Integer maxLength) {}
    public record DdlResult(boolean success, String message, long executionTimeMs) {}
}
