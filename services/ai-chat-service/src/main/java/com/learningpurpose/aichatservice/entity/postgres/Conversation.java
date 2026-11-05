package com.learningpurpose.aichatservice.entity.postgres;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("conversations")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Conversation {

    @Id
    private UUID id;

    private String type;
    private String name;
    private String description;

    @Column("avatar_url")      private String  avatarUrl;
    private String              visibility;
    @Column("invite_code")     private String  inviteCode;
    @Column("workspace_id")    private UUID    workspaceId;
    @Column("created_by")      private Long    createdBy;
    @Column("last_message_at") private Instant lastMessageAt;
    @Column("created_at")      private Instant createdAt;
    @Column("updated_at")      private Instant updatedAt;
    @Column("archived_at")     private Instant archivedAt;
}