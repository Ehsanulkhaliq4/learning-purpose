package com.learningpurpose.platformopsservice.service;

import com.learningpurpose.platformopsservice.config.OpsDatabasesProperties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class DynamicDataSourceService {

    private final OpsDatabasesProperties props;
    private final Map<String, HikariDataSource> pools = new ConcurrentHashMap<>();

    /** Returns a JdbcTemplate bound to the given database. Throws if DB is not whitelisted. */
    public JdbcTemplate forDatabase(String database) {
        if (database == null || database.isBlank()) {
            throw new IllegalArgumentException("Database name is required");
        }
        if (!props.getDatabases().contains(database)) {
            throw new IllegalArgumentException("Database not allowed: " + database);
        }
        HikariDataSource ds = pools.computeIfAbsent(database, this::createPool);
        return new JdbcTemplate(ds);
    }

    private HikariDataSource createPool(String database) {
        OpsDatabasesProperties.Pg pg = props.getPg();
        String url = "jdbc:postgresql://%s:%d/%s".formatted(pg.getHost(), pg.getPort(), database);

        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(url);
        cfg.setUsername(pg.getUser());
        cfg.setPassword(pg.getPassword());
        cfg.setDriverClassName("org.postgresql.Driver");
        cfg.setPoolName("ops-" + database);
        cfg.setMaximumPoolSize(3);       // small — ops only
        cfg.setMinimumIdle(0);
        cfg.setConnectionTimeout(5000);

        log.info("Creating ops connection pool for database: {}", database);
        return new HikariDataSource(cfg);
    }

    @PreDestroy
    public void shutdown() {
        pools.values().forEach(HikariDataSource::close);
        pools.clear();
    }

    public List<String> listAllowedDatabases() {
        return props.getDatabases();
    }
}