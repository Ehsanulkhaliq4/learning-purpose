package com.learningpurpose.platformopsservice.service;

import com.learningpurpose.platformopsservice.dto.ColumnDefinition;
import com.learningpurpose.platformopsservice.dto.DdlResult;
import com.learningpurpose.platformopsservice.dto.TablePage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseSchemaOpsService {

    private final DynamicDataSourceService dataSourceService;

    private static final Pattern SAFE_DDL_PATTERN = Pattern.compile(
            "^(ALTER\\s+TABLE|CREATE\\s+INDEX|DROP\\s+INDEX|COMMENT\\s+ON)\\s+.*",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SAFE_IDENTIFIER = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*$");

    private static final int MAX_LIMIT = 500;
    private static final int DEFAULT_LIMIT = 50;

    public List<String> listDatabases() {
        return dataSourceService.listAllowedDatabases();
    }

    public List<String> listTables(String database) {
        JdbcTemplate jdbc = dataSourceService.forDatabase(database);
        String sql = """
            SELECT table_name
            FROM information_schema.tables
            WHERE table_schema = 'public' AND table_type = 'BASE TABLE'
            ORDER BY table_name;
        """;
        return jdbc.queryForList(sql, String.class);
    }

    public List<ColumnDefinition> getTableSchema(String database, String tableName) {
        JdbcTemplate jdbc = dataSourceService.forDatabase(database);
        String sql = """
            SELECT column_name, data_type, is_nullable, column_default, character_maximum_length
            FROM information_schema.columns
            WHERE table_schema = 'public' AND table_name = ?
            ORDER BY ordinal_position;
        """;

        return jdbc.query(sql, (rs, rowNum) -> new ColumnDefinition(
                rs.getString("column_name"),
                rs.getString("data_type"),
                "YES".equalsIgnoreCase(rs.getString("is_nullable")),
                rs.getString("column_default"),
                rs.getObject("character_maximum_length") != null
                        ? rs.getInt("character_maximum_length")
                        : null
        ), tableName);
    }

    @Transactional
    public DdlResult executeControlledDdl(String database, String ddlQuery) {
        String sanitized = ddlQuery.trim().replaceAll(";+$", "");

        if (!SAFE_DDL_PATTERN.matcher(sanitized).matches()) {
            throw new IllegalArgumentException(
                    "Operation rejected. Only ALTER TABLE, CREATE/DROP INDEX, and COMMENT statements are permitted.");
        }

        JdbcTemplate jdbc = dataSourceService.forDatabase(database);
        long start = System.currentTimeMillis();
        jdbc.execute(sanitized);
        long elapsed = System.currentTimeMillis() - start;

        log.info("[{}] Executed DDL: [{}] in {}ms", database, sanitized, elapsed);
        return new DdlResult(true, "DDL executed successfully", elapsed);
    }

    public TablePage listRows(String database, String tableName,
                              int page, int size, String sortBy, String sortDir) {
        assertSafeIdentifier(tableName, "table name");
        if (size <= 0) size = DEFAULT_LIMIT;
        if (size > MAX_LIMIT) size = MAX_LIMIT;
        if (page < 0) page = 0;

        JdbcTemplate jdbc = dataSourceService.forDatabase(database);

        // 1. Verify table exists
        Long tableExists = jdbc.queryForObject("""
            SELECT COUNT(*) FROM information_schema.tables
            WHERE table_schema = 'public' AND table_name = ?
        """, Long.class, tableName);
        if (tableExists == null || tableExists == 0) {
            throw new IllegalArgumentException("Table not found: " + tableName);
        }

        // 2. Total rows
        Long totalRows = jdbc.queryForObject(
                "SELECT COUNT(*) FROM " + quote(tableName), Long.class);
        long total = totalRows == null ? 0 : totalRows;

        // 3. Build ORDER BY safely
        String orderClause = "";
        if (sortBy != null && !sortBy.isBlank()) {
            assertSafeIdentifier(sortBy, "sort column");
            // confirm the column actually exists on the table
            Long colExists = jdbc.queryForObject("""
                SELECT COUNT(*) FROM information_schema.columns
                WHERE table_schema='public' AND table_name=? AND column_name=?
            """, Long.class, tableName, sortBy);
            if (colExists == null || colExists == 0) {
                throw new IllegalArgumentException("Unknown column: " + sortBy);
            }
            String dir = "desc".equalsIgnoreCase(sortDir) ? "DESC" : "ASC";
            orderClause = " ORDER BY " + quote(sortBy) + " " + dir;
        }

        // 4. Fetch page
        int offset = page * size;
        String sql = "SELECT * FROM " + quote(tableName) + orderClause
                + " LIMIT ? OFFSET ?";

        List<Map<String, Object>> rows = jdbc.queryForList(sql, size, offset);

        // 5. Extract column names from the first row (or via ResultSetMetaData)
        List<String> columns = jdbc.query(
                "SELECT * FROM " + quote(tableName) + " LIMIT 0",
                rs -> {
                    var meta = rs.getMetaData();
                    List<String> cols = new ArrayList<>();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        cols.add(meta.getColumnName(i));
                    }
                    return cols;
                });

        int totalPages = (int) Math.ceil((double) total / size);

        return new TablePage(
                database, tableName,
                columns, rows,
                page, size, total, totalPages
        );
    }

    /* ─────────────── helpers ─────────────── */

    private void assertSafeIdentifier(String value, String label) {
        if (value == null || !SAFE_IDENTIFIER.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid " + label + ": " + value);
        }
    }

    /** Double-quote an identifier for PostgreSQL. */
    private String quote(String identifier) {
        return "\"" + identifier + "\"";
    }

}