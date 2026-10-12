package com.learningpurpose.userservice.repository;

import com.learningpurpose.userservice.model.UserEnrolledItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserEnrolledItemRepository extends JpaRepository<UserEnrolledItem, Long> {
    List<UserEnrolledItem> findByUserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);
}
