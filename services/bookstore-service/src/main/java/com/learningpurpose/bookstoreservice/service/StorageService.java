package com.learningpurpose.bookstoreservice.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

public interface StorageService {
    String uploadFile(MultipartFile file, String folderPrefix);
    InputStream downloadFile(String fileKey);
    String generatePresignedUrl(String fileKey);
    void deleteFile(String fileKey);
}