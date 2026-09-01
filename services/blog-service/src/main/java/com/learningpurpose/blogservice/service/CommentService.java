package com.learningpurpose.blogservice.service;

import com.learningpurpose.blogservice.dto.CommentRequestDto;
import com.learningpurpose.blogservice.dto.CommentResponseDto;

import java.util.List;

public interface CommentService {
    CommentResponseDto createComment(CommentRequestDto request);
    List<CommentResponseDto> getCommentsByPostId(Long postId);
    void deleteComment(Long commentId);
}