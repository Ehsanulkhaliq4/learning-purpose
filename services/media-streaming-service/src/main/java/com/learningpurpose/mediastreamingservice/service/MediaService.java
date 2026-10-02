package com.learningpurpose.mediastreamingservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.learningpurpose.mediastreamingservice.dto.*;

public interface MediaService {
    // Rooms
    JsonNode createRoom(CreateRoomRequest request);
    ActiveRoomsListResponse getAllRooms();
    JsonNode getRoomDetails(String roomId);
    JsonNode deleteRoom(String roomId);

    // Participants
    JsonNode joinParticipant(String roomId, JoinParticipantRequest request);
    ParticipantsListResponse getParticipants(String roomId);
    JsonNode removeParticipant(String roomId, String participantId);

    // Transports
    JsonNode createTransport(String roomId, CreateTransportRequest request);
    JsonNode connectTransport(String roomId, String transportId, ConnectTransportRequest request);
    JsonNode getParticipantTransports(String roomId, String userId);

    // Producers
    JsonNode createProducer(String roomId, CreateProducerRequest request);
    JsonNode pauseProducer(String roomId, String producerId, ActionParticipantRequest request);
    JsonNode resumeProducer(String roomId, String producerId, ActionParticipantRequest request);
    JsonNode deleteProducer(String roomId, String producerId, ActionParticipantRequest request);

    // Consumers
    JsonNode createConsumer(String roomId, CreateConsumerRequest request);
    JsonNode pauseConsumer(String roomId, String consumerId, ActionParticipantRequest request);
    JsonNode resumeConsumer(String roomId, String consumerId, ActionParticipantRequest request);
    JsonNode deleteConsumer(String roomId, String consumerId, ActionParticipantRequest request);

    // Stats
    JsonNode getConferenceStats();
}