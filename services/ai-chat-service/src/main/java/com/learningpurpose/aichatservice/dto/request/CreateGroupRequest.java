package com.learningpurpose.aichatservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.util.List;

@Data
public class CreateGroupRequest {
    @NotBlank private String name;
    private String description;

    @Pattern(regexp = "public|private")
    private String visibility = "private";

    private List<Long> initialMembers;
}