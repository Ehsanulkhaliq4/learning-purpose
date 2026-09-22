package com.learningpurpose.queryservice.dto.blog;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class PostResponse {
    private Long id;
    private String content;
    private String postedBy;
    private Instant createdAt;
    private List<String> tags;
    private String imageStorageKey;
    private long commentCount;
}
