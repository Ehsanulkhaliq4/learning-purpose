package com.learningpurpose.platformopsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ApplicationLogDto {
    private LocalDate logDate;
    private Instant logTime;
    private String serviceName;
    private String logLevel;
    private String message;
    private String containerId;

}
