package com.learningpurpose.aichatservice.service;

import com.learningpurpose.aichatservice.entity.postgres.ChatMember;
import com.learningpurpose.aichatservice.entity.postgres.Conversation;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface DmService {
    Mono<Conversation> getOrCreate(Long a, Long b);

    Mono<ChatMember> ensureCreatorMembership(UUID conversationId, Long userId);
}
