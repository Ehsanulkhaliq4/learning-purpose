package com.learningpurpose.mediastreamingservice.controller;

import com.learningpurpose.mediastreamingservice.dto.*;
import com.learningpurpose.mediastreamingservice.service.MediaService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/media/conference", produces = MediaType.APPLICATION_JSON_VALUE)
public class MediaController {

    private final MediaService mediaService;

    @PostMapping("/rooms")
    public ResponseEntity<JsonNode> createRoom(@RequestBody CreateRoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mediaService.createRoom(request));
    }

    @GetMapping("/rooms")
    public ResponseEntity<ActiveRoomsListResponse> getAllRooms() {
        return ResponseEntity.ok(mediaService.getAllRooms());
    }

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<JsonNode> getRoomDetails(@PathVariable String roomId) {
        return ResponseEntity.ok(mediaService.getRoomDetails(roomId));
    }

    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<JsonNode> deleteRoom(@PathVariable String roomId) {
        return ResponseEntity.ok(mediaService.deleteRoom(roomId));
    }

    // --- Participants ---

    @PostMapping("/rooms/{roomId}/participants")
    public ResponseEntity<JsonNode> joinParticipant(
            @PathVariable String roomId,
            @Valid @RequestBody JoinParticipantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mediaService.joinParticipant(roomId, request));
    }

    @GetMapping("/rooms/{roomId}/participants")
    public ResponseEntity<ParticipantsListResponse> getParticipants(@PathVariable String roomId) {
        return ResponseEntity.ok(mediaService.getParticipants(roomId));
    }

    @DeleteMapping("/rooms/{roomId}/participants/{participantId}")
    public ResponseEntity<JsonNode> removeParticipant(
            @PathVariable String roomId,
            @PathVariable String participantId) {
        return ResponseEntity.ok(mediaService.removeParticipant(roomId, participantId));
    }

    // --- Transports ---

    @PostMapping("/rooms/{roomId}/transports")
    public ResponseEntity<JsonNode> createTransport(
            @PathVariable String roomId,
            @Valid @RequestBody CreateTransportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mediaService.createTransport(roomId, request));
    }

    @PostMapping("/rooms/{roomId}/transports/{transportId}/connect")
    public ResponseEntity<JsonNode> connectTransport(
            @PathVariable String roomId,
            @PathVariable String transportId,
            @Valid @RequestBody ConnectTransportRequest request) {
        return ResponseEntity.ok(mediaService.connectTransport(roomId, transportId, request));
    }

    @GetMapping("/rooms/{roomId}/participants/{userId}/transports")
    public ResponseEntity<JsonNode> getParticipantTransports(
            @PathVariable String roomId,
            @PathVariable String userId) {
        return ResponseEntity.ok(mediaService.getParticipantTransports(roomId, userId));
    }

    // --- Producers ---

    @PostMapping("/rooms/{roomId}/producers")
    public ResponseEntity<JsonNode> createProducer(
            @PathVariable String roomId,
            @Valid @RequestBody CreateProducerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mediaService.createProducer(roomId, request));
    }

    @PostMapping("/rooms/{roomId}/producers/{producerId}/pause")
    public ResponseEntity<JsonNode> pauseProducer(
            @PathVariable String roomId,
            @PathVariable String producerId,
            @Valid @RequestBody ActionParticipantRequest request) {
        return ResponseEntity.ok(mediaService.pauseProducer(roomId, producerId, request));
    }

    @PostMapping("/rooms/{roomId}/producers/{producerId}/resume")
    public ResponseEntity<JsonNode> resumeProducer(
            @PathVariable String roomId,
            @PathVariable String producerId,
            @Valid @RequestBody ActionParticipantRequest request) {
        return ResponseEntity.ok(mediaService.resumeProducer(roomId, producerId, request));
    }

    @DeleteMapping("/rooms/{roomId}/producers/{producerId}")
    public ResponseEntity<JsonNode> deleteProducer(
            @PathVariable String roomId,
            @PathVariable String producerId,
            @Valid @RequestBody ActionParticipantRequest request) {
        return ResponseEntity.ok(mediaService.deleteProducer(roomId, producerId, request));
    }

    // --- Consumers ---

    @PostMapping("/rooms/{roomId}/consumers")
    public ResponseEntity<JsonNode> createConsumer(
            @PathVariable String roomId,
            @Valid @RequestBody CreateConsumerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mediaService.createConsumer(roomId, request));
    }

    @PostMapping("/rooms/{roomId}/consumers/{consumerId}/pause")
    public ResponseEntity<JsonNode> pauseConsumer(
            @PathVariable String roomId,
            @PathVariable String consumerId,
            @Valid @RequestBody ActionParticipantRequest request) {
        return ResponseEntity.ok(mediaService.pauseConsumer(roomId, consumerId, request));
    }

    @PostMapping("/rooms/{roomId}/consumers/{consumerId}/resume")
    public ResponseEntity<JsonNode> resumeConsumer(
            @PathVariable String roomId,
            @PathVariable String consumerId,
            @Valid @RequestBody ActionParticipantRequest request) {
        return ResponseEntity.ok(mediaService.resumeConsumer(roomId, consumerId, request));
    }

    @DeleteMapping("/rooms/{roomId}/consumers/{consumerId}")
    public ResponseEntity<JsonNode> deleteConsumer(
            @PathVariable String roomId,
            @PathVariable String consumerId,
            @Valid @RequestBody ActionParticipantRequest request) {
        return ResponseEntity.ok(mediaService.deleteConsumer(roomId, consumerId, request));
    }

    // --- Stats ---

    @GetMapping("/stats")
    public ResponseEntity<JsonNode> getConferenceStats() {
        return ResponseEntity.ok(mediaService.getConferenceStats());
    }
}
