package com.learningpurpose.userservice.controller;

import com.learningpurpose.userservice.dto.AuthResponse;
import com.learningpurpose.userservice.dto.OtpVerificationRequest;
import com.learningpurpose.userservice.dto.RegisterRequest;
import com.learningpurpose.userservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return new ResponseEntity<>(authService.register(request), HttpStatus.CREATED);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestParam String email) {
        authService.requestPasswordResetOtp(email);
        return ResponseEntity.ok(Map.of("message", "OTP dispatched successfully"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody OtpVerificationRequest request) {
        authService.resetPasswordWithOtp(request);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }
}
