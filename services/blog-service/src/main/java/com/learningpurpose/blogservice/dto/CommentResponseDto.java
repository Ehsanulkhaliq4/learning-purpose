package com.learningpurpose.blogservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponseDto {
    private Long id;
    private Long postId;
    private String content;
    private String postedBy;
    private Instant createdAt;
}