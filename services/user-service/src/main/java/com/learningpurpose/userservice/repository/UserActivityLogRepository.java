package com.learningpurpose.userservice.repository;

import com.learningpurpose.userservice.model.UserActivityLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {
    List<UserActivityLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    long countByUserIdAndActionType(Long userId, String actionType);
    List<UserActivityLog> findTop10ByUserIdAndActionTypeOrderByCreatedAtDesc(Long userId, String actionType);
}
