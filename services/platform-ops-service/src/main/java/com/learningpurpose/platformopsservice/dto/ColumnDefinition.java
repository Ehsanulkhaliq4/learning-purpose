package com.learningpurpose.platformopsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ColumnDefinition {
    private String columnName;
    private String dataType;
    private boolean nullable;
    private String defaultValue;
    private Integer maxLength;
}
