package com.learningpurpose.blogservice.service;

import com.learningpurpose.blogservice.dto.PostRequestDto;
import com.learningpurpose.blogservice.dto.PostResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostService {
    PostResponseDto createPost(PostRequestDto request, MultipartFile image);
    PostResponseDto getPostById(Long id);
    Page<PostResponseDto> getAllPosts(Pageable pageable);
    List<PostResponseDto> searchPosts(String query);
    void likePost(Long id);
    void deletePost(Long id);
}