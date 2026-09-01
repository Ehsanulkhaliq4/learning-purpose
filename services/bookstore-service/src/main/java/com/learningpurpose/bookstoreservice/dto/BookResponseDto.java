package com.learningpurpose.bookstoreservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookResponseDto {
    private Long id;
    private String title;
    private String author;
    private String description;
    private String contentType;
    private Instant postedDate;
    private String coverImageUrl;
    private String pdfDownloadUrl;
}