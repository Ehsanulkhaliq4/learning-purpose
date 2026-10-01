package com.learningpurpose.mediastreamingservice.controller;

import com.learningpurpose.mediastreamingservice.dto.*;
import com.learningpurpose.mediastreamingservice.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/media/conference")
public class MediaController {

    private final MediaService mediaService;

    @PostMapping("/rooms")
    public ResponseEntity<CreateRoomResponse> createRoom(@RequestBody CreateRoomRequest request){
        return ResponseEntity.ok(mediaService.createRoom(request));
    }

    @PostMapping("/rooms/{roomId}/participants")
    public ResponseEntity<JoinParticipantResponse> joinParticipant(@PathVariable String roomId, @RequestBody JoinParticipantRequest request) {
        return ResponseEntity.ok(mediaService.joinParticipant(roomId,request));
    }

    @PostMapping("/rooms/{roomId}/transports")
    public ResponseEntity<CreateTransportResponse> createTransport(@PathVariable String roomId, @RequestBody CreateTransportRequest request) {
        return ResponseEntity.ok(mediaService.createTransport(roomId,request));
    }
}
