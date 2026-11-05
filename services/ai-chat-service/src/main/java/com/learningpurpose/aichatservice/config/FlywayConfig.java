package com.learningpurpose.aichatservice.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

    @Bean(initMethod = "migrate")
    public Flyway flyway(
            @Value("${app.chat.flyway.jdbc-url}") String jdbcUrl,
            @Value("${app.chat.flyway.username}") String username,
            @Value("${app.chat.flyway.password}") String password) {

        return Flyway.configure()
                .dataSource(jdbcUrl, username, password)
                .locations("classpath:db/migration")
                .baselineOnMigrate(false)
                .validateOnMigrate(true)
                .load();
    }
}