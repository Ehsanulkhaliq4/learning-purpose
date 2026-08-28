package com.learningpurpose.examservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryDto {
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
}