package com.learningpurpose.blogservice.controller;

import com.learningpurpose.blogservice.dto.CommentRequestDto;
import com.learningpurpose.blogservice.dto.CommentResponseDto;
import com.learningpurpose.blogservice.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponseDto> createComment(@Valid @RequestBody CommentRequestDto request) {
        return new ResponseEntity<>(commentService.createComment(request), HttpStatus.CREATED);
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentResponseDto>> getCommentsByPost(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsByPostId(postId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}