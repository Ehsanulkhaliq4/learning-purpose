package com.learningpurpose.aichatservice.repository.postgres;

import com.learningpurpose.aichatservice.entity.postgres.Conversation;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ConversationRepository extends ReactiveCrudRepository<Conversation, UUID> {
    Mono<Conversation> findByInviteCode(String inviteCode);

    Flux<Conversation> findByTypeAndVisibility(String type, String visibility);
}