package com.learningpurpose.platformopsservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class ApplicationLog {

    @PrimaryKey
    private ApplicationLogKey key;

    @Column("service_name")
    private String serviceName;

    @Column("log_level")
    private String logLevel;

    @Column("message")
    private String message;

    @Column("container_id")
    private String containerId;

}
