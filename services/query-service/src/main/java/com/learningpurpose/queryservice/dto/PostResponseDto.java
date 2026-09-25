package com.learningpurpose.queryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDto {

    private Long id;

    private String name;

    private String content;

    private String postedBy;

    private String imageStorageKey;

    private List<String> tags;

    private Integer likeCount;

    private Integer viewCount;

    private Instant createdAt;

    private Instant updatedAt;
}