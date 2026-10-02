package com.learningpurpose.mediastreamingservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActiveRoomsListResponse {

    private boolean success;
    private int totalRooms;
    private List<JsonNode> rooms;
}