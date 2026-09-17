package com.learningpurpose.mediastreamingservice.repository;

import com.learningpurpose.mediastreamingservice.model.VideoLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VideoLikeRepository extends JpaRepository<VideoLike, Long> {
    boolean existsByVideoIdAndUsername(Long videoId, String username);
    Optional<VideoLike> findByVideoIdAndUsername(Long videoId, String username);
    void deleteByVideoIdAndUsername(Long videoId, String username);
}
