package com.learningpurpose.blogservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentRequestDto {
    @NotNull(message = "Post ID is required")
    private Long postId;

    @NotBlank(message = "Comment content cannot be empty")
    private String content;

    @NotBlank(message = "Posted by username is required")
    private String postedBy;
}