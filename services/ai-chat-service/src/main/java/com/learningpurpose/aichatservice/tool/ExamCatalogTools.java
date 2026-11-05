package com.learningpurpose.aichatservice.tool;

import com.learningpurpose.aichatservice.dto.ExamQuizSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExamCatalogTools {

    @Value("${app.services.exam-service-url}")
    private String examServiceUrl;

    private final WebClient.Builder webClientBuilder;

    public record QuizQueryResponse(List<ExamQuizSummary> quizzes) {}

    @Tool(name = "getAvailableQuizzes", description = "Fetch all currently active academic quizzes and exams available for student practice")
    public QuizQueryResponse getAvailableQuizzes() {
        log.info("Tool execution: Fetching active quizzes from exam-service");
        try {
            WebClient webClient = webClientBuilder.clone()
                    .baseUrl(examServiceUrl)
                    .build();
            List<ExamQuizSummary> activeQuizzes = webClient.get()
                    .uri("/api/v1/quizzes/active")
                    .retrieve()
                    .bodyToMono(
                            new ParameterizedTypeReference<List<ExamQuizSummary>>() {}
                    )
                    .block();
            return new QuizQueryResponse(
                    activeQuizzes != null
                            ? activeQuizzes
                            : Collections.emptyList()
            );

        } catch (Exception ex) {
            log.error("Failed to query active quizzes from exam-service", ex);

            return new QuizQueryResponse(Collections.emptyList());
        }
    }
}