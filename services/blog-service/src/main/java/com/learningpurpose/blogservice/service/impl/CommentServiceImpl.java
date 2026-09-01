package com.learningpurpose.blogservice.service.impl;

import com.learningpurpose.blogservice.dto.CommentRequestDto;
import com.learningpurpose.blogservice.dto.CommentResponseDto;
import com.learningpurpose.blogservice.exception.ResourceNotFoundException;
import com.learningpurpose.blogservice.model.Comment;
import com.learningpurpose.blogservice.model.Post;
import com.learningpurpose.blogservice.repository.CommentRepository;
import com.learningpurpose.blogservice.repository.PostRepository;
import com.learningpurpose.blogservice.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Override
    @Transactional
    public CommentResponseDto createComment(CommentRequestDto request) {
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Cannot comment: Post not found with id: " + request.getPostId()));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .postedBy(request.getPostedBy())
                .post(post)
                .build();

        Comment saved = commentRepository.save(comment);
        return mapToDto(saved);
    }

    @Override
    public List<CommentResponseDto> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));
        commentRepository.delete(comment);
    }

    private CommentResponseDto mapToDto(Comment comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .content(comment.getContent())
                .postedBy(comment.getPostedBy())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}