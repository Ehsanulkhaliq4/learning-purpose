package com.learningpurpose.userservice.repository;

import com.learningpurpose.userservice.model.UserLearningMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserLearningMetricsRepository extends JpaRepository<UserLearningMetrics, Long> {
    Optional<UserLearningMetrics> findByUserId(Long userId);
}
