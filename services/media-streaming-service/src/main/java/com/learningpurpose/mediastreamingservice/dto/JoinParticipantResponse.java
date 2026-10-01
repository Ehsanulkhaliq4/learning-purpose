package com.learningpurpose.mediastreamingservice.dto;

import lombok.Data;

@Data
public class JoinParticipantResponse {
    private String message;
    private String roomId;
    private String userId;
}
