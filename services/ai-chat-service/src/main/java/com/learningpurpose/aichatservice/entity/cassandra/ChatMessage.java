package com.learningpurpose.aichatservice.entity.cassandra;

import com.learningpurpose.aichatservice.util.Ids;
import lombok.*;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;

import java.time.Instant;
import java.util.UUID;

@Table("messages_by_conversation")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ChatMessage {

    @PrimaryKeyColumn(name = "conversation_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID conversationId;

    @PrimaryKeyColumn(name = "message_id", ordinal = 1,
            type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private UUID messageId;

    @Column("sender_id")   private Long    senderId;
    @Column("type")        private String  type;
    @Column("content")     private String  content;
    @Column("reply_to_id") private UUID    replyToId;
    @Column("created_at")  private Instant createdAt;
    @Column("metadata")    private String  metadata;

    public static ChatMessage create(UUID conversationId, Long senderId,
                                     String content, String type,
                                     String metadata, UUID replyToId) {
        return ChatMessage.builder()
                .conversationId(conversationId)
                .messageId(Ids.timeBased())
                .senderId(senderId)
                .type(type)
                .content(content)
                .replyToId(replyToId)
                .createdAt(Instant.now())
                .metadata(metadata)
                .build();
    }
}