package com.learningpurpose.aichatservice.service;

import com.learningpurpose.aichatservice.dto.ClientMessage;
import com.learningpurpose.aichatservice.entity.cassandra.ChatMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ChatService {
    Mono<ChatMessage> sendMessage(Long senderId, ClientMessage req);
    Mono<Void> markRead(Long userId, ClientMessage req);
    Flux<ChatMessage> history(UUID conversationId, UUID before, int limit);
}
