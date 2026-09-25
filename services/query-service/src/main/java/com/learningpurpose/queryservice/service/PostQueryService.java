package com.learningpurpose.queryservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.learningpurpose.queryservice.document.PostDocument;
import com.learningpurpose.queryservice.dto.PostResponseDto;
import com.learningpurpose.queryservice.repository.PostRepository;

@Service
@RequiredArgsConstructor
public class PostQueryService {

    private final PostRepository postRepository;

    public Page<PostResponseDto> getAllPosts(int page,int size) {
        Pageable pageable = PageRequest.of(page,size,Sort.by(Sort.Direction.DESC, "createdAt"));
        return postRepository.findAll(pageable).map(this::toDto);
    }

    public PostResponseDto getPostById(Long id) {
        PostDocument post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
        return toDto(post);
    }

    private PostResponseDto toDto(PostDocument post) {

        return PostResponseDto.builder()
                .id(post.getId())
                .name(post.getName())
                .content(post.getContent())
                .postedBy(post.getPostedBy())
                .imageStorageKey(post.getImageStorageKey())
                .tags(post.getTags())
                .likeCount(post.getLikeCount())
                .viewCount(post.getViewCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}