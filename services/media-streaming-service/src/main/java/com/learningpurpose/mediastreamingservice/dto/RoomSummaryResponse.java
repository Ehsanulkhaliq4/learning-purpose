package com.learningpurpose.mediastreamingservice.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomSummaryResponse {

    private String roomId;
    private String title;
    private Instant createdAt;
    private String routerId;
    private JsonNode routerRtpCapabilities;
}