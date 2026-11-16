package com.learningpurpose.userservice.repository;

import com.learningpurpose.userservice.model.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findTop20ByUsernameContainingIgnoreCaseOrderByUsernameAsc(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query(value = """
            SELECT u.* FROM users u
            WHERE NOT EXISTS (
                SELECT 1
                FROM user_roles ur
                JOIN roles r ON r.id = ur.role_id
                WHERE ur.user_id = u.id AND r.role_name = 'ROLE_ADMIN'
            )
            ORDER BY u.created_at DESC
            """, nativeQuery = true)
    List<User> findAllNonAdminUsers();

    @Modifying
    @Transactional
    @Query("""
            UPDATE User u
            SET u.enabled = true, u.deactivatedUntil = null
            WHERE u.enabled = false AND u.deactivatedUntil IS NOT NULL
              AND u.deactivatedUntil <= :now
            """)
    int reactivateExpiredAccounts(@Param("now") Instant now);
}
