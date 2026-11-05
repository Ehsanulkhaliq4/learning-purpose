package com.learningpurpose.aichatservice.service.impl;

import com.learningpurpose.aichatservice.entity.postgres.ChatMember;
import com.learningpurpose.aichatservice.entity.postgres.Conversation;
import com.learningpurpose.aichatservice.entity.postgres.DmPair;
import com.learningpurpose.aichatservice.repository.postgres.ChatMemberRepository;
import com.learningpurpose.aichatservice.repository.postgres.ConversationRepository;
import com.learningpurpose.aichatservice.repository.postgres.DmPairRepository;
import com.learningpurpose.aichatservice.service.DmService;
import com.learningpurpose.aichatservice.util.Ids;
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
                .flatMap(p -> conversationRepo.findById(p.getConversationId()))
                .switchIfEmpty(Mono.defer(() -> createNew(a, b)));
    }

    private Mono<Conversation> createNew(Long a, Long b) {
        Instant now = Instant.now();
        Conversation c = Conversation.builder()
                .id(Ids.uuid()).type("dm").createdBy(a)
                .createdAt(now).updatedAt(now).build();

        return conversationRepo.save(c)
                .flatMap(saved -> addMember(saved.getId(), a)
                        .then(addMember(saved.getId(), b))
                        .then(insertPair(a, b, saved.getId(), now))
                        .thenReturn(saved));
    }

    private Mono<ChatMember> addMember(java.util.UUID cid, Long uid) {
        return memberRepo.save(ChatMember.builder()
                .id(Ids.uuid()).conversationId(cid).userId(uid)
                .role("member").status("active").joinedAt(Instant.now())
                .unreadCount(0).build());
    }

    private Mono<DmPair> insertPair(Long a, Long b, java.util.UUID cid, Instant now) {
        Long lo = Math.min(a, b);
        Long hi = Math.max(a, b);
        return dmPairRepo.save(DmPair.builder()
                .id(Ids.uuid()).userAId(lo).userBId(hi)
                .conversationId(cid).createdAt(now).build());
    }
}
