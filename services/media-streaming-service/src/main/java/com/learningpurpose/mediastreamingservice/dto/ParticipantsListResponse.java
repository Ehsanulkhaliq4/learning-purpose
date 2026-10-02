package com.learningpurpose.mediastreamingservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipantsListResponse {
    private boolean success;
    private String roomId;
    private int totalParticipants;
    private List<ParticipantSummaryResponse> participants;
}