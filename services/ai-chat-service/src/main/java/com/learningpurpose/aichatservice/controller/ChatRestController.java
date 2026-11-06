package com.learningpurpose.aichatservice.controller;

import com.learningpurpose.aichatservice.entity.cassandra.ChatMessage;
import com.learningpurpose.aichatservice.entity.postgres.ChatMember;
import com.learningpurpose.aichatservice.entity.postgres.Conversation;
import com.learningpurpose.aichatservice.exception.ForbiddenException;
import com.learningpurpose.aichatservice.repository.postgres.ChatMemberRepository;
import com.learningpurpose.aichatservice.repository.postgres.ConversationRepository;
import com.learningpurpose.aichatservice.service.ChatService;
import com.learningpurpose.aichatservice.service.DmService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat/ws")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService              chatService;
    private final DmService                dmService;
    private final ChatMemberRepository     memberRepo;
    private final ConversationRepository   conversationRepo;

    /** Paginated history — client opens conversation and loads old messages. */
    @GetMapping("/{conversationId}/messages")
    public Flux<ChatMessage> history(@RequestHeader("X-User-Id") Long me,
                                     @PathVariable UUID conversationId,
                                     @RequestParam(required = false) UUID before,
                                     @RequestParam(defaultValue = "50") int limit) {
        return requireActive(conversationId, me)
                .thenMany(chatService.history(conversationId, before, Math.min(limit, 200)));
    }

    /** Chat list — my conversations ordered by last message, with unread counts. */
    @GetMapping("/conversations")
    public Flux<ConversationWithMeta> myConversations(@RequestHeader("X-User-Id") Long me) {
        return memberRepo.findByUserIdAndStatus(me, "active")
                .flatMap(member -> conversationRepo.findById(member.getConversationId())
                        .map(c -> new ConversationWithMeta(c, member.getUnreadCount(),
                                member.getLastReadMessageId())));
    }

    private Mono<ChatMember> requireActive(UUID cid, Long uid) {
        return memberRepo.findByConversationIdAndUserId(cid, uid)
                .filter(m -> "active".equals(m.getStatus()))
                .switchIfEmpty(Mono.defer(() -> dmService.ensureCreatorMembership(cid, uid)
                        .switchIfEmpty(Mono.error(new ForbiddenException("Not a member")))));
    }

    public record ConversationWithMeta(Conversation conversation,
                                       Integer unreadCount,
                                       UUID lastReadMessageId) {}
}