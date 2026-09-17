package com.learningpurpose.aichatservice.config;

import com.learningpurpose.aichatservice.dto.ExamQuizSummary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class ExamCatalogTools {

    @Value("${app.services.exam-service-url}")
    private String examServiceUrl;

    @Tool(description = "Fetch all currently active academic quizzes and exams available for student practice")
    public List<ExamQuizSummary> getAvailableQuizzes() {
        log.info("Tool execution: Fetching active quizzes");
        try {
            WebClient webClient = WebClient.builder().baseUrl(examServiceUrl).build();
            List<ExamQuizSummary> activeQuizzes = webClient.get()
                    .uri("/api/v1/quizzes/active")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<ExamQuizSummary>>() {})
                    .block();

            return activeQuizzes != null ? activeQuizzes : Collections.emptyList();
        } catch (Exception ex) {
            log.error("Failed to query active quizzes from exam-service: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }
}
