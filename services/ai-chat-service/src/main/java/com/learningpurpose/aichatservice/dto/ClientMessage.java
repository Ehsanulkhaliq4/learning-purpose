package com.learningpurpose.aichatservice.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class ClientMessage {
    private String type;              // SEND | READ | PING
    private UUID conversationId;
    private String content;
    private String contentType = "text";
    private String metadata;
    private UUID replyToId;
    private UUID messageId;           // for READ
}