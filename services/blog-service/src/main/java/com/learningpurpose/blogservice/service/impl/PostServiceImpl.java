package com.learningpurpose.blogservice.service.impl;

import com.learningpurpose.blogservice.dto.PostRequestDto;
import com.learningpurpose.blogservice.dto.PostResponseDto;
import com.learningpurpose.blogservice.exception.ResourceNotFoundException;
import com.learningpurpose.blogservice.model.Post;
import com.learningpurpose.blogservice.repository.PostRepository;
import com.learningpurpose.blogservice.service.PostService;
import com.learningpurpose.blogservice.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final StorageService storageService;

    @Override
    @Transactional
    public PostResponseDto createPost(PostRequestDto request, MultipartFile image) {
        String imageKey = storageService.uploadFile(image, "posts");

        Post post = Post.builder()
                .name(request.getName())
                .content(request.getContent())
                .postedBy(request.getPostedBy())
                .imageStorageKey(imageKey)
                .tags(request.getTags() != null ? request.getTags() : new ArrayList<>())
                .build();

        Post saved = postRepository.save(post);
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public PostResponseDto getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));

        postRepository.incrementViewCount(id);
        post.setViewCount(post.getViewCount() + 1);

        return mapToDto(post);
    }

    @Override
    public Page<PostResponseDto> getAllPosts(Pageable pageable) {
        return postRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::mapToDto);
    }

    @Override
    public List<PostResponseDto> searchPosts(String query) {
        return postRepository.findByNameContainingIgnoreCase(query)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void likePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new ResourceNotFoundException("Post not found with id: " + id);
        }
        postRepository.incrementLikeCount(id);
    }

    @Override
    @Transactional
    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));

        storageService.deleteFile(post.getImageStorageKey());
        postRepository.delete(post);
    }

    private PostResponseDto mapToDto(Post post) {
        return PostResponseDto.builder()
                .id(post.getId())
                .name(post.getName())
                .content(post.getContent())
                .postedBy(post.getPostedBy())
                .imageUrl(storageService.generatePresignedUrl(post.getImageStorageKey()))
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .tags(post.getTags())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .commentCount(post.getComments() != null ? post.getComments().size() : 0)
                .build();
    }
}