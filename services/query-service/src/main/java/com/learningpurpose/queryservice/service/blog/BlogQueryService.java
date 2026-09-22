package com.learningpurpose.queryservice.service.blog;

import com.learningpurpose.queryservice.document.blog.Comment;
import com.learningpurpose.queryservice.document.blog.Post;
import com.learningpurpose.queryservice.dto.blog.CommentResponse;
import com.learningpurpose.queryservice.dto.blog.PostResponse;
import com.learningpurpose.queryservice.repository.blog.CommentRepository;
import com.learningpurpose.queryservice.repository.blog.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class BlogQueryService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public Page<PostResponse> listPosts(Pageable pageable) {
        return postRepository.findActivePosts(pageable).map(this::toPostResponse);
    }

    public PostResponse getPost(Long id) {
        Post post = postRepository.findActiveById(id)
                .orElseThrow(() -> new NoSuchElementException("Post not found: " + id));
        return toPostResponse(post);
    }

    public Page<PostResponse> listPostsByUser(String user, Pageable pageable) {
        return postRepository.findActiveByPostedBy(user, pageable).map(this::toPostResponse);
    }

    public Page<PostResponse> listPostsByTag(String tag, Pageable pageable) {
        return postRepository.findActiveByTag(tag, pageable).map(this::toPostResponse);
    }

    public Page<CommentResponse> listCommentsForPost(Long postId, Pageable pageable) {
        return commentRepository.findActiveByPostId(postId, pageable).map(this::toCommentResponse);
    }

    // --- mapping helpers ---

    private PostResponse toPostResponse(Post p) {
        return PostResponse.builder()
                .id(p.getId())
                .content(p.getContent())
                .postedBy(p.getPostedBy())
                .createdAt(p.getCreatedAt())   // parses the String
                .tags(p.getTags())             // parses the String
                .imageStorageKey(p.getImageStorageKey())
                .commentCount(commentRepository.countActiveByPostId(p.getId()))
                .build();
    }

    private CommentResponse toCommentResponse(Comment c) {
        return CommentResponse.builder()
                .id(c.getId())
                .postId(c.getPostId())
                .postedBy(c.getPostedBy())
                .build();
    }
}
