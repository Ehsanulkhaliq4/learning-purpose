package com.learningpurpose.mediastreamingservice.dto;

import lombok.Data;

@Data
public class CreateRoomResponse {
    private String message;
    private String roomId;
    private String routerId;
}
