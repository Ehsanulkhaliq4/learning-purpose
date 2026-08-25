package com.learningpurpose.userservice.repository;

import com.learningpurpose.userservice.model.SaveOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface SaveOtpRepository extends JpaRepository<SaveOtp, Long> {
    Optional<SaveOtp> findTopByEmailAndOtpAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
            String email, String otp, Instant now
    );
}
