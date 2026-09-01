package com.learningpurpose.aichatservice.controller;

import com.learningpurpose.aichatservice.dto.ChatPromptRequest;
import com.learningpurpose.aichatservice.service.AiTutorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AiChatController {

    private final AiTutorService aiTutorService;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@Valid @RequestBody ChatPromptRequest request) {
        return aiTutorService.streamTutorResponse(request.getPrompt(), request.getSubjectContext());
    }
}