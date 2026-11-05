package com.learningpurpose.aichatservice.dto;

import lombok.Builder;
import lombok.Value;
import java.time.Instant;
import java.util.UUID;

@Value @Builder
public class ServerMessage {
    String type;                       // MESSAGE | READ_RECEIPT | MEMBER_REQUEST | ERROR | PONG
    String subType;                    // for MEMBER_REQUEST: JOIN_REQUEST | INVITATION | APPROVED | REJECTED
    UUID   conversationId;
    UUID   messageId;
    Long   senderId;
    String content;
    String contentType;
    String metadata;
    Instant createdAt;

    // MEMBER_REQUEST fields
    UUID    groupId;
    String  groupName;
    Long    requesterId;
    String  requesterName;
    Long    inviterId;

    // READ_RECEIPT
    Long    readerId;
}