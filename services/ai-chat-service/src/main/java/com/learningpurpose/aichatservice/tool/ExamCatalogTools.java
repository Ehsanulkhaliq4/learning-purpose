package com.learningpurpose.aichatservice.tool;

import com.learningpurpose.aichatservice.dto.ExamQuizSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ExamCatalogTools {

    @Value("${app.services.exam-service-url}")
    private String examServiceUrl;

    public record QuizQueryRequest(String categoryTitle) {}
    public record QuizQueryResponse(List<ExamQuizSummary> quizzes) {}

    @Bean
    @Description("Fetch all currently active academic quizzes and exams available for student practice")
    public Function<QuizQueryRequest, QuizQueryResponse> getAvailableQuizzes() {
        return request -> {
            log.info("Tool execution: Fetching active quizzes for category: {}", request.categoryTitle());
            try {
                WebClient webClient = WebClient.builder().baseUrl(examServiceUrl).build();
                List<ExamQuizSummary> activeQuizzes = webClient.get()
                        .uri("/api/v1/quizzes/active")
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<List<ExamQuizSummary>>() {})
                        .block();

                return new QuizQueryResponse(activeQuizzes != null ? activeQuizzes : Collections.emptyList());
            } catch (Exception ex) {
                log.error("Failed to query active quizzes from exam-service: {}", ex.getMessage());
                return new QuizQueryResponse(Collections.emptyList());
            }
        };
    }
}
