package com.learningpurpose.aichatservice.service.impl;

import com.learningpurpose.aichatservice.entity.postgres.ChatMember;
import com.learningpurpose.aichatservice.entity.postgres.Conversation;
import com.learningpurpose.aichatservice.entity.postgres.DmPair;
import com.learningpurpose.aichatservice.repository.postgres.ChatMemberRepository;
import com.learningpurpose.aichatservice.repository.postgres.ConversationRepository;
import com.learningpurpose.aichatservice.repository.postgres.DmPairRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DmServiceImplTest {

    private static final Long USER_ID = 10L;
    private static final Long OTHER_USER_ID = 20L;
    private static final UUID CONVERSATION_ID = UUID.randomUUID();

    @Mock
    private DmPairRepository dmPairRepo;
    @Mock
    private ConversationRepository conversationRepo;
    @Mock
    private ChatMemberRepository memberRepo;

    private DmServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DmServiceImpl(dmPairRepo, conversationRepo, memberRepo);
    }

    @Test
    void reactivatesCurrentUserWhenExistingDmMembershipWasLeft() {
        Conversation conversation = Conversation.builder().id(CONVERSATION_ID).build();
        ChatMember member = ChatMember.builder()
                .conversationId(CONVERSATION_ID)
                .userId(USER_ID)
                .status("left")
                .build();
        stubExistingConversation(conversation);
        when(memberRepo.findByConversationIdAndUserId(CONVERSATION_ID, USER_ID))
                .thenReturn(Mono.just(member));
        when(memberRepo.save(member)).thenReturn(Mono.just(member));

        StepVerifier.create(service.getOrCreate(USER_ID, OTHER_USER_ID))
                .expectNext(conversation)
                .verifyComplete();

        assertEquals("active", member.getStatus());
        assertNotNull(member.getJoinedAt());
        assertNull(member.getLeftAt());
        verify(memberRepo).save(member);
    }

    @Test
    void createsCurrentUserMembershipWhenExistingDmMemberRowIsMissing() {
        Conversation conversation = Conversation.builder().id(CONVERSATION_ID).build();
        stubExistingConversation(conversation);
        when(memberRepo.findByConversationIdAndUserId(CONVERSATION_ID, USER_ID))
                .thenReturn(Mono.empty());
        when(memberRepo.save(any(ChatMember.class))).thenAnswer(invocation ->
                Mono.just(invocation.getArgument(0)));

        StepVerifier.create(service.getOrCreate(USER_ID, OTHER_USER_ID))
                .expectNext(conversation)
                .verifyComplete();

        ArgumentCaptor<ChatMember> memberCaptor = ArgumentCaptor.forClass(ChatMember.class);
        verify(memberRepo).save(memberCaptor.capture());
        ChatMember created = memberCaptor.getValue();
        assertEquals(CONVERSATION_ID, created.getConversationId());
        assertEquals(USER_ID, created.getUserId());
        assertEquals("active", created.getStatus());
        assertNotNull(created.getJoinedAt());
    }

    @Test
    void restoresCreatorMembershipWhenDmHasAnActivePeer() {
        Conversation conversation = Conversation.builder()
                .id(CONVERSATION_ID)
                .type("dm")
                .createdBy(USER_ID)
                .build();
        ChatMember peer = ChatMember.builder().userId(OTHER_USER_ID).status("active").build();
        when(conversationRepo.findById(CONVERSATION_ID)).thenReturn(Mono.just(conversation));
        when(memberRepo.findByConversationIdAndStatus(CONVERSATION_ID, "active"))
                .thenReturn(Flux.just(peer));
        when(memberRepo.findByConversationIdAndUserId(CONVERSATION_ID, USER_ID))
                .thenReturn(Mono.empty());
        when(memberRepo.save(any(ChatMember.class))).thenAnswer(invocation ->
                Mono.just(invocation.getArgument(0)));

        StepVerifier.create(service.ensureCreatorMembership(CONVERSATION_ID, USER_ID))
                .assertNext(member -> {
                    assertEquals(CONVERSATION_ID, member.getConversationId());
                    assertEquals(USER_ID, member.getUserId());
                    assertEquals("active", member.getStatus());
                })
                .verifyComplete();
    }

    @Test
    void doesNotRestoreCreatorMembershipWithoutAnActivePeer() {
        Conversation conversation = Conversation.builder()
                .id(CONVERSATION_ID)
                .type("dm")
                .createdBy(USER_ID)
                .build();
        when(conversationRepo.findById(CONVERSATION_ID)).thenReturn(Mono.just(conversation));
        when(memberRepo.findByConversationIdAndStatus(CONVERSATION_ID, "active"))
                .thenReturn(Flux.empty());

        StepVerifier.create(service.ensureCreatorMembership(CONVERSATION_ID, USER_ID))
                .verifyComplete();

        verify(memberRepo, never()).save(any(ChatMember.class));
    }

    @Test
    void doesNotAllowNonCreatorToRejoinDmWithoutMembership() {
        Conversation conversation = Conversation.builder()
                .id(CONVERSATION_ID)
                .type("dm")
                .createdBy(USER_ID)
                .build();
        when(conversationRepo.findById(CONVERSATION_ID)).thenReturn(Mono.just(conversation));

        StepVerifier.create(service.ensureCreatorMembership(CONVERSATION_ID, OTHER_USER_ID))
                .verifyComplete();

        verify(memberRepo, never()).save(any(ChatMember.class));
    }

    @Test
    void createsNewDmWhenPairDoesNotExist() {
        Conversation savedConversation = Conversation.builder()
                .id(CONVERSATION_ID)
                .type("dm")
                .createdBy(USER_ID)
                .build();

        when(dmPairRepo.findBetween(USER_ID, OTHER_USER_ID)).thenReturn(Mono.empty());
        when(conversationRepo.save(any(Conversation.class))).thenReturn(Mono.just(savedConversation));
        when(memberRepo.save(any(ChatMember.class))).thenAnswer(invocation ->
                Mono.just(invocation.getArgument(0)));
        when(dmPairRepo.save(any(DmPair.class))).thenAnswer(invocation ->
                Mono.just(invocation.getArgument(0)));

        StepVerifier.create(service.getOrCreate(USER_ID, OTHER_USER_ID))
                .expectNext(savedConversation)
                .verifyComplete();

        ArgumentCaptor<Conversation> convCaptor = ArgumentCaptor.forClass(Conversation.class);
        verify(conversationRepo).save(convCaptor.capture());
        assertNull(convCaptor.getValue().getId(), "New conversation must have null ID for database generation");
        assertEquals("dm", convCaptor.getValue().getType());
        assertEquals(USER_ID, convCaptor.getValue().getCreatedBy());

        ArgumentCaptor<ChatMember> memberCaptor = ArgumentCaptor.forClass(ChatMember.class);
        verify(memberRepo, times(2)).save(memberCaptor.capture());
        for (ChatMember member : memberCaptor.getAllValues()) {
            assertNull(member.getId(), "New chat member must have null ID for database generation");
            assertEquals(CONVERSATION_ID, member.getConversationId());
            assertEquals("active", member.getStatus());
        }

        ArgumentCaptor<DmPair> pairCaptor = ArgumentCaptor.forClass(DmPair.class);
        verify(dmPairRepo).save(pairCaptor.capture());
        assertNull(pairCaptor.getValue().getId(), "New DM pair must have null ID for database generation");
        assertEquals(CONVERSATION_ID, pairCaptor.getValue().getConversationId());
        assertEquals(Math.min(USER_ID, OTHER_USER_ID), pairCaptor.getValue().getUserAId());
        assertEquals(Math.max(USER_ID, OTHER_USER_ID), pairCaptor.getValue().getUserBId());
    }

    private void stubExistingConversation(Conversation conversation) {
        DmPair pair = DmPair.builder().conversationId(CONVERSATION_ID).build();
        when(dmPairRepo.findBetween(USER_ID, OTHER_USER_ID)).thenReturn(Mono.just(pair));
        when(conversationRepo.findById(CONVERSATION_ID)).thenReturn(Mono.just(conversation));
    }
}
