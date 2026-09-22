package com.learningpurpose.queryservice.dto.blog;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentResponse {
    private Long id;
    private Long postId;
    private String postedBy;
}
