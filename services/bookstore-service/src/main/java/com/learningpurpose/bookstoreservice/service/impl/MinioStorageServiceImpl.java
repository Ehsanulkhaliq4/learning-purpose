package com.learningpurpose.bookstoreservice.service.impl;

import com.learningpurpose.bookstoreservice.exception.FileStorageException;
import com.learningpurpose.bookstoreservice.service.StorageService;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioStorageServiceImpl implements StorageService{

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;


    @Override
    public String uploadFile(MultipartFile file, String folderPrefix) {
        if (file == null || file.isEmpty()){
            return null;
        }
        String extension = "";
        String originalFilename = file.getOriginalFilename();
        if(originalFilename != null && originalFilename.contains(".")){
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String objectKey = folderPrefix + "/" + UUID.randomUUID() + extension;
        try (InputStream is =file.getInputStream()){
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .stream(is, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            return objectKey;
        }catch (Exception e) {
            log.error("Failed to upload object {} to MinIO: {}", objectKey, e.getMessage());
            throw new FileStorageException("File upload failed for " + originalFilename, e);
        }
    }

    @Override
    public InputStream downloadFile(String fileKey) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileKey)
                            .build()
            );
        }catch (Exception e) {
            throw new FileStorageException("Could not retrieve file: " + fileKey, e);
        }
    }

    @Override
    public String generatePresignedUrl(String fileKey) {
        if (fileKey == null || fileKey.isBlank()) {
            return null;
        }
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(fileKey)
                            .expiry(2, TimeUnit.HOURS)
                            .build()
            );
        }catch (Exception e) {
            log.warn("Failed to generate presigned URL for key {}: {}", fileKey, e.getMessage());
            return null;
        }
    }

    @Override
    public void deleteFile(String fileKey) {
        if (fileKey == null || fileKey.isBlank()) {
            return;
        }
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileKey)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to delete object {}: {}", fileKey, e.getMessage());
        }
    }
}
