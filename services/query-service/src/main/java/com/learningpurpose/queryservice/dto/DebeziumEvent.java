package com.learningpurpose.queryservice.dto;

import lombok.Data;
import tools.jackson.databind.JsonNode;

@Data
public class DebeziumEvent {

    private JsonNode before;

    private JsonNode after;

    private JsonNode source;

    private String op;
}