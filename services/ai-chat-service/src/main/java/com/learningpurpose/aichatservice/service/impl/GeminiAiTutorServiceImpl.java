package com.learningpurpose.aichatservice.service.impl;

import com.learningpurpose.aichatservice.service.AiTutorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiAiTutorServiceImpl implements AiTutorService {

    private final ChatClient chatClient;

    @Override
    public Flux<String> streamTutorResponse(String prompt, String subjectContext) {
        String effectiveContext = (subjectContext != null && !subjectContext.isBlank())
                ? subjectContext
                : "General Academic Curriculum";

        log.info("Initiating Gemini token stream for prompt context: [{}]", effectiveContext);

        return chatClient.prompt()
                .system(sp -> sp.param("context", effectiveContext))
                .user(prompt)
                .stream()
                .content()
                .onErrorResume(ex -> {
                    log.error("Gemini stream connection error: {}", ex.getMessage());
                    return Flux.just("\n[Streaming error: " + ex.getMessage() + "]");
                });
    }
}