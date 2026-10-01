package com.learningpurpose.mediastreamingservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class TransportDto {
    private String id;
    private String direction;
    private IceParametersDto iceParameters;
    private List<IceCandidateDto> iceCandidates;
    private DtlsParametersDto dtlsParameters;
}
