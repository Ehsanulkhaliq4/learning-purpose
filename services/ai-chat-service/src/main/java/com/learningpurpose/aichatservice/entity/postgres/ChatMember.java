package com.learningpurpose.aichatservice.entity.postgres;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("chat_members")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ChatMember {

    @Id
    private UUID id;

    @Column("conversation_id")      private UUID    conversationId;
    @Column("user_id")              private Long    userId;
    private String                  role;
    private String                  status;
    @Column("joined_at")            private Instant joinedAt;
    @Column("left_at")              private Instant leftAt;
    @Column("invited_by")           private Long    invitedBy;
    @Column("last_read_message_id") private UUID    lastReadMessageId;
    @Column("last_read_at")         private Instant lastReadAt;
    @Column("unread_count")         private Integer unreadCount;
}