package com.learningpurpose.platformopsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DdlResult {
    private boolean success;
    private String message;
    private long executionTimeMs;
}
