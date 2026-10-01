package com.learningpurpose.mediastreamingservice.dto;

import lombok.Data;

@Data
public class CreateTransportResponse {
    private String message;
    private TransportDto transport;
}
