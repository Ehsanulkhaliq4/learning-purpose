package com.learningpurpose.aichatservice.service.impl;

import com.learningpurpose.aichatservice.entity.postgres.ChatMember;
import com.learningpurpose.aichatservice.entity.postgres.Conversation;
import com.learningpurpose.aichatservice.entity.postgres.Invite;
import com.learningpurpose.aichatservice.exception.ConflictException;
import com.learningpurpose.aichatservice.exception.ForbiddenException;
import com.learningpurpose.aichatservice.exception.NotFoundException;
import com.learningpurpose.aichatservice.repository.postgres.ChatMemberRepository;
import com.learningpurpose.aichatservice.repository.postgres.ConversationRepository;
import com.learningpurpose.aichatservice.repository.postgres.InviteRepository;
import com.learningpurpose.aichatservice.service.GroupService;
import com.learningpurpose.aichatservice.service.MemberRequestNotificationService;
import com.learningpurpose.aichatservice.util.Ids;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {
    private final ConversationRepository conversationRepo;
    private final ChatMemberRepository memberRepo;
    private final InviteRepository inviteRepo;
    private final MemberRequestNotificationService notifier;
    @Override
    public Mono<Conversation> create(Long creatorId, String name, String description, String visibility) {
        Instant now = Instant.now();
        Conversation c = Conversation.builder()
                .id(Ids.uuid()).type("group").name(name)
                .description(description).visibility(visibility)
                .inviteCode(Ids.inviteCode()).createdBy(creatorId)
                .createdAt(now).updatedAt(now).build();

        return conversationRepo.save(c)
                .flatMap(saved -> addMember(saved.getId(), creatorId, "owner", "active")
                        .thenReturn(saved));
    }

    @Override
    public Mono<Void> requestJoin(UUID groupId, Long userId, String userName) {
        return conversationRepo.findById(groupId)
                .switchIfEmpty(Mono.error(new NotFoundException("Group not found")))
                .flatMap(group ->
                        memberRepo.findByConversationIdAndUserId(groupId, userId)
                                .flatMap(existing -> Mono.<Void>error(
                                        new ConflictException("Already " + existing.getStatus())))
                                .switchIfEmpty(Mono.defer(() ->
                                        addMember(groupId, userId, "member", "pending")
                                                .then(notifier.pushJoinRequest(
                                                        groupId, group.getName(), userId, userName))
                                ))
                );
    }

    @Override
    public Mono<Void> approve(UUID groupId, Long requesterId, Long adminId) {
        return requireAdmin(groupId, adminId)
                .then(memberRepo.findByConversationIdAndUserId(groupId, requesterId))
                .switchIfEmpty(Mono.error(new NotFoundException("No pending request")))
                .flatMap(m -> {
                    if (!"pending".equals(m.getStatus())) {
                        return Mono.error(new ConflictException("Not pending"));
                    }
                    m.setStatus("active");
                    m.setJoinedAt(Instant.now());
                    return memberRepo.save(m);
                })
                .then(conversationRepo.findById(groupId))
                .flatMap(group -> notifier.pushDecision(
                        requesterId, groupId, group.getName(), true));
    }

    @Override
    public Mono<Void> reject(UUID groupId, Long requesterId, Long adminId) {
        return requireAdmin(groupId, adminId)
                .then(memberRepo.findByConversationIdAndUserId(groupId, requesterId))
                .switchIfEmpty(Mono.error(new NotFoundException("No pending request")))
                .flatMap(memberRepo::delete)
                .then(conversationRepo.findById(groupId))
                .flatMap(group -> notifier.pushDecision(
                        requesterId, groupId, group.getName(), false));
    }

    @Override
    public Mono<Invite> invite(UUID groupId, Long inviterId, Long inviteeId) {
        return conversationRepo.findById(groupId)
                .switchIfEmpty(Mono.error(new NotFoundException("Group not found")))
                .flatMap(group -> {
                    Invite inv = Invite.builder()
                            .id(Ids.uuid()).type("group").conversationId(groupId)
                            .inviterId(inviterId).inviteeId(inviteeId)
                            .status("pending").createdAt(Instant.now())
                            .expiresAt(Instant.now().plusSeconds(7 * 86400))
                            .build();
                    return inviteRepo.save(inv)
                            .flatMap(saved -> notifier.pushInvitation(
                                            inviteeId, inviterId, group.getName())
                                    .thenReturn(saved));
                });
    }

    @Override
    public Mono<Void> leave(UUID groupId, Long userId) {
        return memberRepo.findByConversationIdAndUserId(groupId, userId)
                .switchIfEmpty(Mono.error(new NotFoundException("Not a member")))
                .flatMap(m -> {
                    m.setStatus("left");
                    m.setLeftAt(Instant.now());
                    return memberRepo.save(m);
                })
                .then(memberRepo.countActive(groupId))
                .flatMap(count -> {
                    if (count == 0) {
                        // Last active member — destroy conversation
                        // Cassandra messages remain (can be purged by scheduled job)
                        return conversationRepo.deleteById(groupId);
                    }
                    return Mono.empty();
                });
    }

    // ─────────────────────────── Helpers ───────────────────────────
    private Mono<ChatMember> addMember(UUID cid, Long uid, String role, String status) {
        return memberRepo.save(ChatMember.builder()
                .id(Ids.uuid()).conversationId(cid).userId(uid)
                .role(role).status(status)
                .joinedAt("active".equals(status) ? Instant.now() : null)
                .unreadCount(0).build());
    }

    private Mono<Void> requireAdmin(UUID cid, Long uid) {
        return memberRepo.findByConversationIdAndUserId(cid, uid)
                .filter(m -> "owner".equals(m.getRole()) || "admin".equals(m.getRole()))
                .switchIfEmpty(Mono.error(new ForbiddenException("Admin only")))
                .then();
    }
}
