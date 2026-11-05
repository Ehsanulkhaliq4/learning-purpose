package com.learningpurpose.aichatservice.controller;

import com.learningpurpose.aichatservice.entity.postgres.Conversation;
import com.learningpurpose.aichatservice.service.DmService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/chat/dm")
@RequiredArgsConstructor
public class DmRestController {

    private final DmService dmService;

    @GetMapping("/{otherUserId}")
    public Mono<Conversation> getOrCreate(@RequestHeader("X-User-Id") Long me,
                                          @PathVariable Long otherUserId) {
        return dmService.getOrCreate(me, otherUserId);
    }
}