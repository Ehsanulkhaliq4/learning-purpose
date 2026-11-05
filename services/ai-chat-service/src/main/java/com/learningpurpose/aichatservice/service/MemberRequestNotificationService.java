package com.learningpurpose.aichatservice.service;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface MemberRequestNotificationService {
    Mono<Void> pushJoinRequest(UUID groupId, String groupName,Long requesterId, String requesterName);
    Mono<Void> pushDecision(Long requesterId, UUID groupId,String groupName, boolean approved);
    Mono<Void> pushInvitation(Long inviteeId, Long inviterId, String groupName);
}
