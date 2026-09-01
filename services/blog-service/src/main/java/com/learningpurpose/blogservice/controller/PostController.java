package com.learningpurpose.blogservice.controller;

import com.learningpurpose.blogservice.dto.PostRequestDto;
import com.learningpurpose.blogservice.dto.PostResponseDto;
import com.learningpurpose.blogservice.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponseDto> createPost(
            @Valid @RequestPart("post") PostRequestDto request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return new ResponseEntity<>(postService.createPost(request, image), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDto> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @GetMapping
    public ResponseEntity<Page<PostResponseDto>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(postService.getAllPosts(PageRequest.of(page, size)));
    }

    @GetMapping("/search")
    public ResponseEntity<List<PostResponseDto>> searchPosts(@RequestParam String query) {
        return ResponseEntity.ok(postService.searchPosts(query));
    }

    @PutMapping("/{id}/like")
    public ResponseEntity<Void> likePost(@PathVariable Long id) {
        postService.likePost(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}