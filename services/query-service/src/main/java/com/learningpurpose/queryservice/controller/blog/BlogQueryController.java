package com.learningpurpose.queryservice.controller.blog;

import com.learningpurpose.queryservice.dto.blog.CommentResponse;
import com.learningpurpose.queryservice.dto.blog.PostResponse;
import com.learningpurpose.queryservice.service.blog.BlogQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/query")
@RequiredArgsConstructor
public class BlogQueryController {

    private final BlogQueryService service;

    @GetMapping("/posts")
    public ResponseEntity<Page<PostResponse>> listPosts(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.listPosts(pageable(page, size)));
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long id) {
        return ResponseEntity.ok(service.getPost(id));
    }

    @GetMapping("/posts/by-user/{user}")
    public ResponseEntity<Page<PostResponse>> postsByUser(
            @PathVariable String user,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.listPostsByUser(user, pageable(page, size)));
    }

    @GetMapping("/posts/by-tag/{tag}")
    public ResponseEntity<Page<PostResponse>> postsByTag(
            @PathVariable String tag,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.listPostsByTag(tag, pageable(page, size)));
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<Page<CommentResponse>> commentsForPost(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.listCommentsForPost(postId, pageable(page, size)));
    }

    private Pageable pageable(int page, int size) {
        return PageRequest.of(page, Math.min(size, 100),
                Sort.by(Sort.Direction.DESC, "created_at"));
    }
}
