package com.learningpurpose.blogservice.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String uploadFile(MultipartFile file, String folderPrefix);
    String generatePresignedUrl(String fileKey);
    void deleteFile(String fileKey);
}