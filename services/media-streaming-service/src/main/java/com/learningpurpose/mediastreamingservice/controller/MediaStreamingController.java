package com.learningpurpose.mediastreamingservice.controller;

import com.learningpurpose.mediastreamingservice.model.Video;
import com.learningpurpose.mediastreamingservice.service.MediaStreamingService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaStreamingController {

    private final MediaStreamingService mediaService;

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @GetMapping("/videos")
    public ResponseEntity<List<Video>> getCatalog() {
        return ResponseEntity.ok(mediaService.getAllVideos());
    }

    @PostMapping(value = "/videos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Video> uploadVideo(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("file") MultipartFile file,
            Principal principal) {
        String uploader = (principal != null) ? principal.getName() : "admin";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mediaService.uploadVideo(title, description, file, uploader));
    }

    @DeleteMapping("/videos/{id}")
    public ResponseEntity<Void> deleteVideo(@PathVariable Long id) {
        mediaService.deleteVideo(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/videos/{id}/stream")
    public void streamVideo(@PathVariable Long id,
                            @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader,
                            HttpServletResponse response) throws IOException {

        Video video = mediaService.getVideoMetadata(id);
        long totalLength = video.getFileSize();

        long start = 0;
        long end = totalLength - 1;
        boolean isRange = rangeHeader != null && !rangeHeader.isBlank();

        if (isRange) {
            List<HttpRange> ranges = HttpRange.parseRanges(rangeHeader);
            HttpRange range = ranges.get(0);
            start = range.getRangeStart(totalLength);
            end = range.getRangeEnd(totalLength);
        }
        long rangeLength = end - start + 1;

        response.setContentType(video.getContentType());
        response.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");
        response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(rangeLength));

        if (isRange) {
            response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
            response.setHeader(HttpHeaders.CONTENT_RANGE,
                    "bytes " + start + "-" + end + "/" + totalLength);
        } else {
            response.setStatus(HttpStatus.OK.value());
        }

        try (InputStream in = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(video.getObjectName())
                        .offset(start)
                        .length(rangeLength)
                        .build());
             OutputStream out = response.getOutputStream()) {
            in.transferTo(out);
        } catch (Exception e) {
            throw new IOException("MinIO stream failed", e);
        }
    }

    @PostMapping("/videos/{id}/views")
    public ResponseEntity<Void> recordView(@PathVariable Long id) {
        mediaService.incrementViews(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/videos/{id}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable Long id, Principal principal) {
        String username = (principal != null) ? principal.getName() : "anonymous";
        boolean liked = mediaService.toggleLike(id, username);
        return ResponseEntity.ok(Map.of("videoId", id, "liked", liked));
    }

    @GetMapping("/videos/{id}/like-status")
    public ResponseEntity<Map<String, Boolean>> getLikeStatus(@PathVariable Long id, Principal principal) {
        String username = (principal != null) ? principal.getName() : "anonymous";
        boolean liked = mediaService.isLikedByUser(id, username);
        return ResponseEntity.ok(Map.of("liked", liked));
    }
}
