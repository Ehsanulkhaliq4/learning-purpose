package com.learningpurpose.aichatservice.controller;

import com.learningpurpose.aichatservice.dto.request.CreateGroupRequest;
import com.learningpurpose.aichatservice.entity.postgres.Conversation;
import com.learningpurpose.aichatservice.entity.postgres.Invite;
import com.learningpurpose.aichatservice.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat/groups")
@RequiredArgsConstructor
public class GroupRestController {

    private final GroupService groupService;

    @PostMapping
    public Mono<Conversation> create(@RequestHeader("X-User-Id") Long me,
                                     @Valid @RequestBody CreateGroupRequest req) {
        return groupService.create(me, req.getName(), req.getDescription(),
                req.getVisibility());
    }

    @PostMapping("/{id}/join")
    public Mono<Void> requestJoin(@RequestHeader("X-User-Id") Long me,
                                  @PathVariable UUID id,
                                  @RequestParam String userName) {
        return groupService.requestJoin(id, me, userName);
    }

    @PostMapping("/{id}/approve/{userId}")
    public Mono<Void> approve(@RequestHeader("X-User-Id") Long adminId,
                              @PathVariable UUID id,
                              @PathVariable Long userId) {
        return groupService.approve(id, userId, adminId);
    }

    @PostMapping("/{id}/reject/{userId}")
    public Mono<Void> reject(@RequestHeader("X-User-Id") Long adminId,
                             @PathVariable UUID id,
                             @PathVariable Long userId) {
        return groupService.reject(id, userId, adminId);
    }

    @PostMapping("/{id}/invite/{userId}")
    public Mono<Invite> invite(@RequestHeader("X-User-Id") Long me,
                               @PathVariable UUID id,
                               @PathVariable Long userId) {
        return groupService.invite(id, me, userId);
    }

    @PostMapping("/{id}/leave")
    public Mono<Void> leave(@RequestHeader("X-User-Id") Long me,
                            @PathVariable UUID id) {
        return groupService.leave(id, me);
    }
}