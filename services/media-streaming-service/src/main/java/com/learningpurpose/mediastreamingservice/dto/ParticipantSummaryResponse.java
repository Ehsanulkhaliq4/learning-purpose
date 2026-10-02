package com.learningpurpose.mediastreamingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantSummaryResponse {

    private String id;
    private String displayName;
    private Instant joinedAt;
    private boolean isMutedAudio;
    private boolean isMutedVideo;
    private boolean isScreenSharing;
    private boolean isHandRaised;
    private int transportCount;
    private int producerCount;
    private int consumerCount;
}