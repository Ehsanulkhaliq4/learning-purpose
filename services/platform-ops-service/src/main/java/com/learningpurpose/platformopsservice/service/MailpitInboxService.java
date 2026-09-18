package com.learningpurpose.platformopsservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailpitInboxService {

    @Value("${ops.mailpit.base-url}")
    private String mailpitBaseUrl;

    private RestClient client() {
        return RestClient.builder().baseUrl(mailpitBaseUrl).build();
    }

    /** List all messages (paginated). */
    public InboxPage listMessages(int page, int size) {
        Map<String, Object> response = client().get()
                .uri(uri -> uri.path("/api/v1/messages")
                        .queryParam("page", page)
                        .queryParam("limit", size)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        if (response == null) {
            return new InboxPage(List.of(), 0, page, size, 0);
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> messages =
                (List<Map<String, Object>>) response.getOrDefault("messages", List.of());

        int total = response.get("total") != null
                ? ((Number) response.get("total")).intValue() : messages.size();

        return new InboxPage(messages, total, page, size, (int) Math.ceil((double) total / size));
    }

    /** Get a single message by ID — headers, body, attachments. */
    public Map<String, Object> getMessage(String id) {
        return client().get()
                .uri("/api/v1/message/{id}", id)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    /** Raw HTML body. */
    public String getHtmlBody(String id) {
        try {
            return client().get()
                    .uri("/api/v1/message/{id}/html", id)
                    .retrieve()
                    .body(String.class);
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound ex) {
            log.warn("Message {} has no HTML part", id);
            return null;
        }
    }

    /** Raw plain-text body. */
    public String getTextBody(String id) {
        try {
            return client().get()
                    .uri("/api/v1/message/{id}/text", id)
                    .retrieve()
                    .body(String.class);
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound ex) {
            log.warn("Message {} has no text part", id);
            return null;
        }
    }

    /** Delete every message in the inbox. */
    public void purgeInbox() {
        client().delete()
                .uri("/api/v1/messages")
                .retrieve()
                .toBodilessEntity();
    }

    public record InboxPage(List<Map<String, Object>> messages,
                            int total, int page, int size, int totalPages) {}
}