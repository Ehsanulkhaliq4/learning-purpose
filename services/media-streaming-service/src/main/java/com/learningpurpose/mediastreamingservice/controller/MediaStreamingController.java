package com.learningpurpose.mediastreamingservice.controller;

import com.learningpurpose.mediastreamingservice.model.Video;
import com.learningpurpose.mediastreamingservice.service.MediaStreamingService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaStreamingController {

    private final MediaStreamingService mediaService;

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
    public ResponseEntity<ResourceRegion> streamVideo(
            @PathVariable Long id,
            @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader) {

        Video video = mediaService.getVideoMetadata(id);
        List<HttpRange> ranges = HttpRange.parseRanges(rangeHeader);
        HttpRange range = ranges.isEmpty() ? null : ranges.get(0);

        ResourceRegion region = mediaService.streamVideo(id, range);

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaType.parseMediaType(video.getContentType()))
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .body(region);
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
