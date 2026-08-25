package com.learningpurpose.userservice.service;

import com.learningpurpose.userservice.dto.AuthRequest;
import com.learningpurpose.userservice.dto.AuthResponse;
import com.learningpurpose.userservice.dto.OtpVerificationRequest;
import com.learningpurpose.userservice.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(AuthRequest request);
    void requestPasswordResetOtp(String email);
    void resetPasswordWithOtp(OtpVerificationRequest request);
}
