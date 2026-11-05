package com.learningpurpose.aichatservice.repository.cassandra;

import com.learningpurpose.aichatservice.entity.cassandra.ChatMessage;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface MessageRepository
        extends ReactiveCassandraRepository<ChatMessage, UUID> {

    @Query("SELECT * FROM messages_by_conversation " +
            "WHERE conversation_id = ?0 ORDER BY message_id DESC LIMIT ?1")
    Flux<ChatMessage> firstPage(UUID conversationId, int limit);

    @Query("SELECT * FROM messages_by_conversation " +
            "WHERE conversation_id = ?0 AND message_id < ?1 " +
            "ORDER BY message_id DESC LIMIT ?2")
    Flux<ChatMessage> pageBefore(UUID conversationId, UUID before, int limit);
}