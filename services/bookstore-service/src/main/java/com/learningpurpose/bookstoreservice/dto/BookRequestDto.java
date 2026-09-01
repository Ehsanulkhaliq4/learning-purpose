package com.learningpurpose.bookstoreservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BookRequestDto {
    @NotBlank(message = "Book title is required")
    private String title;

    @NotBlank(message = "Author name is required")
    private String author;

    private String description;
    private String contentType;
}