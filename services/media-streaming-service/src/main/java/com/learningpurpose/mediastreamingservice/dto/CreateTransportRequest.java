package com.learningpurpose.mediastreamingservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransportRequest {

    @NotBlank(message = "userId is required")
    private String userId;
    @NotBlank(message = "direction must be 'send' or 'recv'")
    @Pattern(regexp = "^(send|recv)$",message = "direction must be 'send' or 'recv'")
    private String direction;
}