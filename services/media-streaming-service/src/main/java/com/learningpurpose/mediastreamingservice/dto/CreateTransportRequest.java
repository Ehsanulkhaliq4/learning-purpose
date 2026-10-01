package com.learningpurpose.mediastreamingservice.dto;

import lombok.Data;

@Data
public class CreateTransportRequest {
    private String userId;
    private String direction;
}
