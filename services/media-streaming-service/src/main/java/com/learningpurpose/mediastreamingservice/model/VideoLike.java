package com.learningpurpose.mediastreamingservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "video_likes", uniqueConstraints = {
        @UniqueConstraint(name = "uq_video_user_like", columnNames = {"video_id", "username"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VideoLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @Column(nullable = false, length = 150)
    private String username;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public VideoLike(Video video, String username) {
        this.video = video;
        this.username = username;
    }
}
