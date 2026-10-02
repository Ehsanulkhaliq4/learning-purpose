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
public class ConnectTransportRequest {

    @NotBlank(message = "userId is required")
    private String userId;

    private JsonNode dtlsParameters;
}