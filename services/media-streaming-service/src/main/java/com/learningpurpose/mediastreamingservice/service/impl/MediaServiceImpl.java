package com.learningpurpose.mediastreamingservice.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.learningpurpose.mediastreamingservice.dto.*;
import com.learningpurpose.mediastreamingservice.service.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaServiceImpl implements MediaService {
    private final WebClient mediasoupWebClient;
    private final ObjectMapper objectMapper;

    /**
     * Centralized HTTP error handler mapping Node SFU errors to reactive failures.
     */
    private <T> Mono<T> executeRequest(WebClient.ResponseSpec responseSpec, Class<T> responseType) {
        return responseSpec
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .defaultIfEmpty("No error details returned from SFU")
                                .flatMap(errorBody -> {
                                    log.error("SFU Node Error [HTTP {}]: {}", clientResponse.statusCode(), errorBody);
                                    return Mono.error(new RuntimeException("SFU Node Error [" + clientResponse.statusCode() + "]: " + errorBody));
                                })
                )
                .bodyToMono(responseType)
                .doOnError(WebClientRequestException.class, ex ->
                        log.error("Unable to reach Mediasoup Node SFU: {}", ex.getMessage())
                );
    }

    private JsonNode buildFallbackErrorJson(String operation, String message) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("success", false);
        node.put("operation", operation);
        node.put("error", message);
        return node;
    }

    // ==========================================
    // 1. Room Operations (roomRoutes.js)
    // ==========================================

    @Override
    public JsonNode createRoom(CreateRoomRequest request) {
        try {
            return executeRequest(
                    mediasoupWebClient.post()
                            .uri("/api/rooms")
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(request)
                            .retrieve(),
                    JsonNode.class
            ).block();
        } catch (Exception ex) {
            log.error("Error creating room: {}", ex.getMessage());
            return buildFallbackErrorJson("createRoom", ex.getMessage());
        }
    }

    @Override
    public ActiveRoomsListResponse getAllRooms() {
        try {
            return executeRequest(
                    mediasoupWebClient.get()
                            .uri("/api/rooms")
                            .retrieve(),
                    ActiveRoomsListResponse.class
            ).block();
        } catch (Exception ex) {
            log.error("Error fetching rooms: {}", ex.getMessage());
            return ActiveRoomsListResponse.builder()
                    .success(false)
                    .totalRooms(0)
                    .rooms(Collections.emptyList())
                    .build();
        }
    }

    @Override
    public JsonNode getRoomDetails(String roomId) {
        try {
            return executeRequest(
                    mediasoupWebClient.get()
                            .uri("/api/rooms/{roomId}", roomId)
                            .retrieve(),
                    JsonNode.class
            ).block();
        } catch (Exception ex) {
            log.error("Error getting room details for {}: {}", roomId, ex.getMessage());
            return buildFallbackErrorJson("getRoomDetails", ex.getMessage());
        }
    }

    @Override
    public JsonNode deleteRoom(String roomId) {
        try {
            return executeRequest(
                    mediasoupWebClient.delete()
                            .uri("/api/rooms/{roomId}", roomId)
                            .retrieve(),
                    JsonNode.class
            ).block();
        } catch (Exception ex) {
            log.error("Error deleting room {}: {}", roomId, ex.getMessage());
            return buildFallbackErrorJson("deleteRoom", ex.getMessage());
        }
    }

    // ==========================================
    // 2. Participant Operations (participantRoutes.js)
    // ==========================================

    @Override
    public JsonNode joinParticipant(String roomId, JoinParticipantRequest request) {
        try {
            return executeRequest(
                    mediasoupWebClient.post()
                            .uri("/api/rooms/{roomId}/participants", roomId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(request)
                            .retrieve(),
                    JsonNode.class
            ).block();
        } catch (Exception ex) {
            log.error("Error joining participant to room {}: {}", roomId, ex.getMessage());
            return buildFallbackErrorJson("joinParticipant", ex.getMessage());
        }
    }

    @Override
    public ParticipantsListResponse getParticipants(String roomId) {
        try {
            return executeRequest(
                    mediasoupWebClient.get()
                            .uri("/api/rooms/{roomId}/participants", roomId)
                            .retrieve(),
                    ParticipantsListResponse.class
            ).block();
        } catch (Exception ex) {
            log.error("Error fetching participants for room {}: {}", roomId, ex.getMessage());
            return ParticipantsListResponse.builder()
                    .success(false)
                    .roomId(roomId)
                    .totalParticipants(0)
                    .participants(Collections.emptyList())
                    .build();
        }
    }

    @Override
    public JsonNode removeParticipant(String roomId, String participantId) {
        try {
            return executeRequest(
                    mediasoupWebClient.delete()
                            .uri("/api/rooms/{roomId}/participants/{participantId}", roomId, participantId)
                            .retrieve(),
                    JsonNode.class
            ).block();
        } catch (Exception ex) {
            log.error("Error removing participant {} from room {}: {}", participantId, roomId, ex.getMessage());
            return buildFallbackErrorJson("removeParticipant", ex.getMessage());
        }
    }

    // ==========================================
    // 3. WebRTC Transport Operations (transportRoutes.js)
    // ==========================================

    @Override
    public JsonNode createTransport(String roomId, CreateTransportRequest request) {
        try {
            return executeRequest(
                    mediasoupWebClient.post()
                            .uri("/api/rooms/{roomId}/transports", roomId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(request)
                            .retrieve(),
                    JsonNode.class
            ).block();
        } catch (Exception ex) {
            log.error("Error creating transport in room {}: {}", roomId, ex.getMessage());
            return buildFallbackErrorJson("createTransport", ex.getMessage());
        }
    }

    @Override
    public JsonNode connectTransport(String roomId, String transportId, ConnectTransportRequest request) {
        try {
            return executeRequest(
                    mediasoupWebClient.post()
                            .uri("/api/rooms/{roomId}/transports/{transportId}/connect", roomId, transportId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(request)
                            .retrieve(),
                    JsonNode.class
            ).block();
        } catch (Exception ex) {
            log.error("Error connecting transport {} in room {}: {}", transportId, roomId, ex.getMessage());
            return buildFallbackErrorJson("connectTransport", ex.getMessage());
        }
    }

    @Override
    public JsonNode getParticipantTransports(String roomId, String userId) {
        try {
            return executeRequest(
                    mediasoupWebClient.get()
                            .uri("/api/rooms/{roomId}/participants/{userId}/transports", roomId, userId)
                            .retrieve(),
                    JsonNode.class
            ).block();
        } catch (Exception ex) {
            log.error("Error fetching transports for user {} in room {}: {}", userId, roomId, ex.getMessage());
            return buildFallbackErrorJson("getParticipantTransports", ex.getMessage());
        }
    }

    // ==========================================
    // 4. Producer Operations (producerRoutes.js)
    // ==========================================

    @Override
    public JsonNode createProducer(String roomId, CreateProducerRequest request) {
        try {
            return executeRequest(
                    mediasoupWebClient.post()
                            .uri("/api/rooms/{roomId}/producers", roomId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(request)
                            .retrieve(),
                    JsonNode.class
            ).block();
        } catch (Exception ex) {
            log.error("Error creating producer in room {}: {}", roomId, ex.getMessage());
            return buildFallbackErrorJson("createProducer", ex.getMessage());
        }
    }

    @Override
    public JsonNode pauseProducer(String roomId, String producerId, ActionParticipantRequest request) {
        try {
            return executeRequest(
                    mediasoupWebClient.post()
                            .uri("/api/rooms/{roomId}/producers/{producerId}/pause", roomId, producerId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(request)
                            .retrieve(),
                    JsonNode.class
            ).block();
        } catch (Exception ex) {
            log.error("Error pausing producer {} in room {}: {}", producerId, roomId, ex.getMessage());
            return buildFallbackErrorJson("pauseProducer", ex.getMessage());
        }
    }

    @Override
    public JsonNode resumeProducer(String roomId, String producerId, ActionParticipantRequest request) {
        try {
            return executeRequest(
                    mediasoupWebClient.post()
                            .uri("/api/rooms/{roomId}/producers/{producerId}/resume", roomId, producerId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(request)
                            .retrieve(),
                    JsonNode.class
            ).block();
        } catch (Exception ex) {
            log.error("Error resuming producer {} in room {}: {}", producerId, roomId, ex.getMessage());
            return buildFallbackErrorJson("resumeProducer", ex.getMessage());
        }
    }

    @Override
    public JsonNode deleteProducer(String roomId, String producerId, ActionParticipantRequest request) {
        try {
            return executeRequest(
                    mediasoupWebClient.method(HttpMethod.DELETE)
                            .uri("/api/rooms/{roomId}/producers/{producerId}", roomId, producerId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(request)
                            .retrieve(),
                    JsonNode.class
            ).block();
        } catch (Exception ex) {
            log.error("Error deleting producer {} in room {}: {}", producerId, roomId, ex.getMessage());
            return buildFallbackErrorJson("deleteProducer", ex.getMessage());
        }
    }

    // ==========================================
    // 5. Consumer Operations (consumerRoutes.js)
    // ==========================================

    @Override
    public JsonNode createConsumer(String roomId, CreateConsumerRequest request) {
        try {
            return executeRequest(
                    mediasoupWebClient.post()
                            .uri("/api/rooms/{roomId}/consumers", roomId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(request)
                            .retrieve(),
                    JsonNode.class
            ).block();
        } catch (Exception ex) {
            log.error("Error creating consumer in room {}: {}", roomId, ex.getMessage());
            return buildFallbackErrorJson("createConsumer", ex.getMessage());
        }
    }

    @Override
    public JsonNode pauseConsumer(String roomId, String consumerId, ActionParticipantRequest request) {
        try {
            return executeRequest(
                    mediasoupWebClient.post()
                            .uri("/api/rooms/{roomId}/consumers/{consumerId}/pause", roomId, consumerId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(request)
                            .retrieve(),
                    JsonNode.class
            ).block();
        } catch (Exception ex) {
            log.error("Error pausing consumer {} in room {}: {}", consumerId, roomId, ex.getMessage());
            return buildFallbackErrorJson("pauseConsumer", ex.getMessage());
        }
    }

    @Override
    public JsonNode resumeConsumer(String roomId, String consumerId, ActionParticipantRequest request) {
        try {
            return executeRequest(
                    mediasoupWebClient.post()
                            .uri("/api/rooms/{roomId}/consumers/{consumerId}/resume", roomId, consumerId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(request)
                            .retrieve(),
                    JsonNode.class
            ).block();
        } catch (Exception ex) {
            log.error("Error resuming consumer {} in room {}: {}", consumerId, roomId, ex.getMessage());
            return buildFallbackErrorJson("resumeConsumer", ex.getMessage());
        }
    }

    @Override
    public JsonNode deleteConsumer(String roomId, String consumerId, ActionParticipantRequest request) {
        try {
            return executeRequest(
                    mediasoupWebClient.method(HttpMethod.DELETE)
                            .uri("/api/rooms/{roomId}/consumers/{consumerId}", roomId, consumerId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(request)
                            .retrieve(),
                    JsonNode.class
            ).block();
        } catch (Exception ex) {
            log.error("Error deleting consumer {} in room {}: {}", consumerId, roomId, ex.getMessage());
            return buildFallbackErrorJson("deleteConsumer", ex.getMessage());
        }
    }

    // ==========================================
    // 6. SFU Diagnostic Stats (statsRoutes.js)
    // ==========================================

    @Override
    public JsonNode getConferenceStats() {
        try {
            return executeRequest(
                    mediasoupWebClient.get()
                            .uri("/api/stats")
                            .retrieve(),
                    JsonNode.class
            ).block();
        } catch (Exception ex) {
            log.error("Error fetching SFU statistics: {}", ex.getMessage());
            return buildFallbackErrorJson("getConferenceStats", ex.getMessage());
        }
    }
}