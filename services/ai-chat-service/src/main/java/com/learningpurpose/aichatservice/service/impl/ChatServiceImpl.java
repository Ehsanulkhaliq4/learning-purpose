package com.learningpurpose.aichatservice.service.impl;

import com.google.api.client.util.Value;
import com.learningpurpose.aichatservice.dto.ClientMessage;
import com.learningpurpose.aichatservice.dto.ServerMessage;
import com.learningpurpose.aichatservice.entity.cassandra.ChatMessage;
import com.learningpurpose.aichatservice.exception.ForbiddenException;
import com.learningpurpose.aichatservice.repository.cassandra.MessageRepository;
import com.learningpurpose.aichatservice.repository.postgres.ChatMemberRepository;
import com.learningpurpose.aichatservice.repository.postgres.ConversationRepository;
import com.learningpurpose.aichatservice.service.ChatService;
import com.learningpurpose.aichatservice.websocket.SessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final MessageRepository messageRepo;
    private final ConversationRepository conversationRepo;
    private final ChatMemberRepository memberRepo;
    private final SessionRegistry registry;
    private final ObjectMapper mapper;

    @Value("${app.chat.message.max-content-length:10000}")
    private int maxContentLength;

    @Override
    public Mono<ChatMessage> sendMessage(Long senderId, ClientMessage req) {
        if (req.getContent() == null || req.getContent().isBlank()) {
            return Mono.error(new IllegalArgumentException("Empty message"));
        }

        if (req.getContent().length() > maxContentLength) {
            return Mono.error(new IllegalArgumentException(
                    "Message exceeds " + maxContentLength + " chars"));
        }

        UUID cid = req.getConversationId();

        return memberRepo.findByConversationIdAndUserId(cid,senderId)
                .filter(m -> "active".equals(m.getStatus()))
                .switchIfEmpty(Mono.error(new ForbiddenException("Not an active member")))
                .flatMap(m -> messageRepo.save(ChatMessage.create(
                        cid,senderId,req.getContent(),
                        req.getContentType(), req.getMetadata(), req.getReplyToId())))
                .flatMap(saved -> bumpConversion(saved)
                .thenMany(incrementUnreadForOthers(saved))
                        .then().thenReturn(saved))
                .flatMap(saved -> broadcast(saved).thenReturn(saved));
    }

    private Mono<Void> bumpConversion(ChatMessage msg){
        return conversationRepo.findById(msg.getConversationId())
                .flatMap(c -> {
                    c.setLastMessageAt(msg.getCreatedAt());
                    return conversationRepo.save(c);
                }).then();
    }

    private Flux<Integer> incrementUnreadForOthers(ChatMessage msg){
        return memberRepo.findActiveUserIdsExcept(msg.getConversationId(), msg.getSenderId())
                .flatMap(uid -> memberRepo.incrementUnread(msg.getConversationId(),uid));
    }

    private Mono<Void> broadcast(ChatMessage saved){
        ServerMessage payload = ServerMessage.builder()
                .type("MESSAGE")
                .conversationId(saved.getConversationId())
                .messageId(saved.getMessageId())
                .senderId(saved.getSenderId())
                .content(saved.getContent())
                .contentType(saved.getType())
                .metadata(saved.getMetadata())
                .createdAt(saved.getCreatedAt())
                .build();
        String json = serialize(payload);
        return memberRepo.findActiveUserIdsExcept(saved.getConversationId(), -1L)
                .flatMap(uid -> registry.sendToUser(uid, json))
                .then();
    }

    @Override
    public Mono<Void> markRead(Long userId, ClientMessage req) {
        UUID cid = req.getConversationId();
        UUID mid = req.getMessageId();
        return memberRepo.markRead(cid, userId, mid)
                .then(Mono.defer(() -> {
                    ServerMessage receipt = ServerMessage.builder()
                            .type("READ_RECEIPT")
                            .conversationId(cid)
                            .messageId(mid)
                            .readerId(userId)
                            .build();
                    String json = serialize(receipt);
                    return memberRepo.findActiveUserIdsExcept(cid, userId)
                            .flatMap(uid -> registry.sendToUser(uid, json))
                            .then();
                }));
    }

    @Override
    public Flux<ChatMessage> history(UUID conversationId, UUID before, int limit) {
        return before == null
                ? messageRepo.firstPage(conversationId, limit)
                : messageRepo.pageBefore(conversationId, before, limit);
    }

    @SneakyThrows
    private String serialize(ServerMessage m){
        return mapper.writeValueAsString(m);
    }
}
