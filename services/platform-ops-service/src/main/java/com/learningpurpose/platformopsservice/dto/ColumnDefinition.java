package com.learningpurpose.platformopsservice.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@Builder
public class ColumnDefinition {
    private String columnName;
    private String dataType;
    private boolean nullable;
    private String defaultValue;
    private Integer maxLength;
}
