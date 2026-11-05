package com.learningpurpose.aichatservice.entity.postgres;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("dm_pairs")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DmPair {

    @Id
    private UUID id;

    @Column("user_a_id")       private Long    userAId;
    @Column("user_b_id")       private Long    userBId;
    @Column("conversation_id") private UUID    conversationId;
    @Column("created_at")      private Instant createdAt;
}