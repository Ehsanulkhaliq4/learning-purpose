package com.learningpurpose.mediastreamingservice.dto;

import lombok.Data;

@Data
public class IceCandidateDto {
    private String foundation;
    private Long priority;
    private String ip;
    private String address;
    private String protocol;
    private Integer port;
    private String type;
    private String tcpType;
}
