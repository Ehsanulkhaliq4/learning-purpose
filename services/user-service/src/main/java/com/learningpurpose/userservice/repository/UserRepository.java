package com.learningpurpose.userservice.repository;

import com.learningpurpose.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findTop20ByUsernameContainingIgnoreCaseOrderByUsernameAsc(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
