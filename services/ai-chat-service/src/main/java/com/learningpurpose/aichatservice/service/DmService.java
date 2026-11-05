package com.learningpurpose.aichatservice.service;

import com.learningpurpose.aichatservice.entity.postgres.Conversation;
import reactor.core.publisher.Mono;

public interface DmService {
    Mono<Conversation> getOrCreate(Long a, Long b);
}
