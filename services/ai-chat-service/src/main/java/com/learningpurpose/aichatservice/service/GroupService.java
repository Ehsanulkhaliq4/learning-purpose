package com.learningpurpose.aichatservice.service;

import com.learningpurpose.aichatservice.entity.postgres.Conversation;
import com.learningpurpose.aichatservice.entity.postgres.Invite;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GroupService {
    Mono<Conversation> create(Long creatorId, String name, String description, String visibility);
    Mono<Void> requestJoin(UUID groupId, Long userId, String userName);
    Mono<Void> approve(UUID groupId, Long requesterId, Long adminId);
    Mono<Void> reject(UUID groupId, Long requesterId, Long adminId);
    Mono<Invite> invite(UUID groupId, Long inviterId, Long inviteeId);
    Mono<Void> leave(UUID groupId, Long userId);

}
