package com.learningpurpose.platformopsservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "ops")
@Data
public class OpsDatabasesProperties {

    private List<String> databases;
    private Pg pg = new Pg();

    @Data
    public static class Pg {
        private String host;
        private int port;
        private String user;
        private String password;
    }
}