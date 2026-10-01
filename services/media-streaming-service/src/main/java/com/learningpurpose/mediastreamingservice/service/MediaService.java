package com.learningpurpose.mediastreamingservice.service;

import com.learningpurpose.mediastreamingservice.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final RestClient mediasoupRestClient;

    public CreateRoomResponse createRoom(CreateRoomRequest request){
        return mediasoupRestClient.post().uri("/api/rooms").body(request).retrieve().body(CreateRoomResponse.class);
    }

    public JoinParticipantResponse joinParticipant(String roomId, JoinParticipantRequest request){
        return mediasoupRestClient.post().uri("/api/rooms/{roomId}/participants",roomId).body(request).retrieve().body(JoinParticipantResponse.class);
    }

    public CreateTransportResponse createTransport(String roomId, CreateTransportRequest request){
        return mediasoupRestClient.post().uri( "/api/rooms/{roomId}/transports",roomId).body(request).retrieve().body(CreateTransportResponse.class);
    }
}
