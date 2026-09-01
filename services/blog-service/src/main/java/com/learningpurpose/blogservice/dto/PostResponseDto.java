package com.learningpurpose.blogservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostResponseDto {
    private Long id;
    private String name;
    private String content;
    private String postedBy;
    private String imageUrl;
    private int viewCount;
    private int likeCount;
    private List<String> tags;
    private Instant createdAt;
    private Instant updatedAt;
    private int commentCount;
}