package com.learningpurpose.mediastreamingservice.dto;

import lombok.Data;

@Data
public class IceParametersDto {
    private String usernameFragment;
    private String password;
    private Boolean iceLite;
}
