package com.learningpurpose.blogservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class PostRequestDto {
    @NotBlank(message = "Post title is required")
    private String name;

    @NotBlank(message = "Content cannot be empty")
    private String content;

    @NotBlank(message = "Author is required")
    private String postedBy;

    private List<String> tags;
}