package com.learningpurpose.mediastreamingservice.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateConsumerRequest {

    @NotBlank(message = "userId is required")
    private String userId;

    @NotBlank(message = "transportId is required")
    private String transportId;

    @NotBlank(message = "producerId is required")
    private String producerId;

    private JsonNode rtpCapabilities;
}