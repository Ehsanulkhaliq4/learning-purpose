package com.learningpurpose.apigateway.config;

import com.learningpurpose.apigateway.filter.JwtAuthenticationGatewayFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GatewayRoutesConfig {

    private final JwtAuthenticationGatewayFilter jwtFilter;

    @Value("${services.user-service}")
    private String userServiceUrl;

    @Value("${services.exam-service}")
    private String examServiceUrl;

    @Value("${services.bookstore-service}")
    private String bookstoreServiceUrl;

    @Value("${services.blog-service}")
    private String blogServiceUrl;

    @Value("${services.ai-chat-service}")
    private String aiChatServiceUrl;

    @Value("${services.media-streaming-service}")
    private String mediaServiceUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // 1. User & Authentication Route
                .route("user-service", r -> r.path("/api/v1/auth/**", "/api/v1/users/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri(userServiceUrl))

                // 2. Exam, Categories & Questions Route
                .route("exam-service", r -> r.path("/api/v1/categories/**", "/api/v1/quizzes/**", "/api/v1/questions/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri(examServiceUrl))

                // 3. Bookstore & Catalog Route
                .route("bookstore-service", r -> r.path("/api/v1/books/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri(bookstoreServiceUrl))

                // 4. Community Blog & Comments Route
                .route("blog-service", r -> r.path("/api/v1/posts/**", "/api/v1/comments/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri(blogServiceUrl))

                // 5. Spring AI Real-Time Chat SSE Stream Route
                .route("ai-chat-service", r -> r.path("/api/v1/chat/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri(aiChatServiceUrl))

                // 6. Non-blocking Media & Video Byte-Range Route
                .route("media-streaming-service", r -> r.path("/api/v1/media/**")
                        .filters(f -> f.filter(jwtFilter))
                        .uri(mediaServiceUrl))

                .build();
    }
}
