package com.learningpurpose.mediastreamingservice.service;

import com.learningpurpose.mediastreamingservice.model.Video;
import com.learningpurpose.mediastreamingservice.model.VideoLike;
import com.learningpurpose.mediastreamingservice.repository.VideoLikeRepository;
import com.learningpurpose.mediastreamingservice.repository.VideoRepository;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpRange;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaStreamingService {

    private final VideoRepository videoRepository;
    private final VideoLikeRepository videoLikeRepository;
    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    private static final long CHUNK_SIZE = 1024 * 1024; // 1MB chunk per byte range request

    @Transactional(readOnly = true)
    public List<Video> getAllVideos() {
        return videoRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Video getVideoMetadata(Long id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Video not found with id: " + id));
    }

    @Transactional
    public Video uploadVideo(String title, String description, MultipartFile file, String uploadedBy) {
        String objectName = "lectures/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (Exception e) {
            log.error("MinIO file upload failed", e);
            throw new RuntimeException("Storage upload error: " + e.getMessage(), e);
        }

        Video video = Video.builder()
                .title(title)
                .description(description)
                .objectName(objectName)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .viewCount(0L)
                .likeCount(0L)
                .uploadedBy(uploadedBy)
                .build();

        return videoRepository.save(video);
    }

    @Transactional
    public void deleteVideo(Long id) {
        Video video = getVideoMetadata(id);
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(video.getObjectName())
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to delete object from MinIO", e);
            throw new RuntimeException("Storage delete error: " + e.getMessage(), e);
        }
        videoRepository.delete(video);
    }

    @Transactional
    public void incrementViews(Long id) {
        videoRepository.incrementViewCount(id);
    }

    @Transactional
    public boolean toggleLike(Long videoId, String username) {
        Video video = getVideoMetadata(videoId);
        return videoLikeRepository.findByVideoIdAndUsername(videoId, username)
                .map(existing -> {
                    videoLikeRepository.delete(existing);
                    videoRepository.decrementLikeCount(videoId);
                    return false;
                })
                .orElseGet(() -> {
                    videoLikeRepository.save(new VideoLike(video, username));
                    videoRepository.incrementLikeCount(videoId);
                    return true;
                });
    }

    @Transactional(readOnly = true)
    public boolean isLikedByUser(Long videoId, String username) {
        return videoLikeRepository.existsByVideoIdAndUsername(videoId, username);
    }

    public ResourceRegion streamVideo(Long id, HttpRange range) {
        Video video = getVideoMetadata(id);
        long totalLength = video.getFileSize();

        long start = 0;
        long end = totalLength - 1;

        if (range != null) {
            start = range.getRangeStart(totalLength);
            end = range.getRangeEnd(totalLength);
        }

        long rangeLength = Math.min(CHUNK_SIZE, end - start + 1);

        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(video.getObjectName())
                            .offset(start)
                            .length(rangeLength)
                            .build()
            );

            Resource resource = new InputStreamResource(stream);
            return new ResourceRegion(resource, 0, rangeLength);
        } catch (Exception e) {
            log.error("Failed reading range: bytes={}-{} for video id: {}", start, end, id, e);
            throw new RuntimeException("Stream retrieval failed: " + e.getMessage(), e);
        }
    }
}
