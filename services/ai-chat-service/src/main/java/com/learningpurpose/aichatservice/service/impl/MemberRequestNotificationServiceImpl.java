package com.learningpurpose.aichatservice.service.impl;

import com.learningpurpose.aichatservice.dto.ServerMessage;
import com.learningpurpose.aichatservice.repository.postgres.ChatMemberRepository;
import com.learningpurpose.aichatservice.service.MemberRequestNotificationService;
import com.learningpurpose.aichatservice.websocket.SessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberRequestNotificationServiceImpl implements MemberRequestNotificationService {

    private final SessionRegistry registry;
    private final ChatMemberRepository memberRepo;
    private final ObjectMapper mapper;


    @Override
    public Mono<Void> pushJoinRequest(UUID groupId, String groupName, Long requesterId, String requesterName) {
        ServerMessage event = ServerMessage.builder()
                .type("MEMBER_REQUEST").subType("JOIN_REQUEST")
                .groupId(groupId).groupName(groupName)
                .requesterId(requesterId).requesterName(requesterName)
                .createdAt(Instant.now()).build();
        return broadcastToAdmins(groupId, event);
    }

    @Override
    public Mono<Void> pushDecision(Long requesterId, UUID groupId, String groupName, boolean approved) {
        ServerMessage event = ServerMessage.builder()
                .type("MEMBER_REQUEST")
                .subType(approved ? "APPROVED" : "REJECTED")
                .groupId(groupId).groupName(groupName)
                .createdAt(Instant.now()).build();
        return registry.sendToUser(requesterId, serialize(event));
    }

    @Override
    public Mono<Void> pushInvitation(Long inviteeId, Long inviterId, String groupName) {
        ServerMessage event = ServerMessage.builder()
                .type("MEMBER_REQUEST").subType("INVITATION")
                .groupName(groupName).inviterId(inviterId)
                .createdAt(Instant.now()).build();
        return registry.sendToUser(inviteeId, serialize(event));
    }

    private Mono<Void> broadcastToAdmins(UUID groupId, ServerMessage event) {
        String json = serialize(event);
        return memberRepo.findAdminUserIds(groupId)
                .flatMap(adminId -> registry.sendToUser(adminId, json))
                .then();
    }

    @SneakyThrows
    private String serialize(ServerMessage m) {
        return mapper.writeValueAsString(m);
    }
}
