package com.learningpurpose.aichatservice.entity.postgres;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("invites")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Invite {

    @Id
    private UUID id;

    private String type;
    @Column("conversation_id") private UUID    conversationId;
    @Column("inviter_id")      private Long    inviterId;
    @Column("invitee_id")      private Long    inviteeId;
    private String             status;
    @Column("created_at")      private Instant createdAt;
    @Column("expires_at")      private Instant expiresAt;
}