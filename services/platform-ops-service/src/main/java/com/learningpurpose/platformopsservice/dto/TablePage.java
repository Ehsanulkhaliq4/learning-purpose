package com.learningpurpose.platformopsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TablePage {
    private String database;
    private String table;
    private List<String> columns;
    private List<Map<String, Object>> rows;
    private int page;
    private int size;
    private long totalRows;
    private int totalPages;
}
