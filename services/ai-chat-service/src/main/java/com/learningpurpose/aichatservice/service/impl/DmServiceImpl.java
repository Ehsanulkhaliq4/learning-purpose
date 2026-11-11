package com.learningpurpose.aichatservice.service.impl;

import com.learningpurpose.aichatservice.entity.postgres.ChatMember;
import com.learningpurpose.aichatservice.entity.postgres.Conversation;
import com.learningpurpose.aichatservice.entity.postgres.DmPair;
import com.learningpurpose.aichatservice.exception.ForbiddenException;
import com.learningpurpose.aichatservice.repository.postgres.ChatMemberRepository;
import com.learningpurpose.aichatservice.repository.postgres.ConversationRepository;
import com.learningpurpose.aichatservice.repository.postgres.DmPairRepository;
import com.learningpurpose.aichatservice.service.DmService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class DmServiceImpl implements DmService {

    private final DmPairRepository dmPairRepo;
    private final ConversationRepository conversationRepo;
    private final ChatMemberRepository memberRepo;


    @Override
    @Transactional
    public Mono<Conversation> getOrCreate(Long a, Long b) {
        if (a.equals(b)) {
            return Mono.error(new IllegalArgumentException("Cannot DM yourself"));
        }
        return dmPairRepo.findBetween(a, b)
                .flatMap(p -> conversationRepo.findById(p.getConversationId())
                        .flatMap(conversation -> ensureActiveMember(conversation.getId(), a)
                                .thenReturn(conversation)))
                .switchIfEmpty(Mono.defer(() -> createNew(a, b)));
    }

    @Override
    public Mono<ChatMember> ensureCreatorMembership(java.util.UUID cid, Long uid) {
        return conversationRepo.findById(cid)
                .filter(conversation -> "dm".equalsIgnoreCase(conversation.getType())
                        && uid.equals(conversation.getCreatedBy()))
                .flatMap(conversation -> memberRepo.findByConversationIdAndStatus(cid, "active")
                        .filter(member -> !uid.equals(member.getUserId()))
                        .hasElements()
                        .filter(Boolean::booleanValue)
                        .flatMap(hasActivePeer -> memberRepo.findByConversationIdAndUserId(cid, uid)
                                .flatMap(member -> {
                                    if ("active".equals(member.getStatus())) {
                                        return Mono.just(member);
                                    }
                                    if ("left".equals(member.getStatus())) {
                                        member.setStatus("active");
                                        member.setJoinedAt(Instant.now());
                                        member.setLeftAt(null);
                                        return memberRepo.save(member);
                                    }
                                    return Mono.error(new ForbiddenException("Not an active member"));
                                })
                                .switchIfEmpty(Mono.defer(() -> addMember(cid, uid)))));
    }

    private Mono<ChatMember> ensureActiveMember(java.util.UUID cid, Long uid) {
        return memberRepo.findByConversationIdAndUserId(cid, uid)
                .flatMap(member -> {
                    if ("active".equals(member.getStatus())) {
                        return Mono.just(member);
                    }
                    if ("left".equals(member.getStatus())) {
                        member.setStatus("active");
                        member.setJoinedAt(Instant.now());
                        member.setLeftAt(null);
                        return memberRepo.save(member);
                    }
                    return Mono.error(new ForbiddenException("Not an active member"));
                })
                .switchIfEmpty(Mono.defer(() -> addMember(cid, uid)));
    }

    private Mono<Conversation> createNew(Long a, Long b) {
        Instant now = Instant.now();
        Conversation c = Conversation.builder()
                .type("dm").createdBy(a)
                .createdAt(now).updatedAt(now).build();

        return conversationRepo.save(c)
                .flatMap(saved -> addMember(saved.getId(), a)
                        .then(addMember(saved.getId(), b))
                        .then(insertPair(a, b, saved.getId(), now))
                        .thenReturn(saved));
    }

    private Mono<ChatMember> addMember(java.util.UUID cid, Long uid) {
        return memberRepo.save(ChatMember.builder()
                .conversationId(cid).userId(uid)
                .role("member").status("active").joinedAt(Instant.now())
                .unreadCount(0).build());
    }

    private Mono<DmPair> insertPair(Long a, Long b, java.util.UUID cid, Instant now) {
        Long lo = Math.min(a, b);
        Long hi = Math.max(a, b);
        return dmPairRepo.save(DmPair.builder()
                .userAId(lo).userBId(hi)
                .conversationId(cid).createdAt(now).build());
    }
}
