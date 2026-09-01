package com.learningpurpose.aichatservice.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiChatConfig {

    private static final String SYSTEM_PROMPT = """
        You are the AI Academic Tutor for the Learning Purpose platform.
        Your mission is to mentor students through exam preparation, quizzes, concepts, and study schedules.
        
        Rules:
        1. Break down technical and academic concepts systematically using bullet points and worked examples.
        2. If a student asks what exams or quizzes are available, use the `getAvailableQuizzes` tool to retrieve live data.
        3. Do not disclose direct exam answer keys without guiding the user to solve them first.
        4. Maintain an encouraging, precise, and academic tone.
        """;

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultFunctions("getAvailableQuizzes")
                .build();
    }
}