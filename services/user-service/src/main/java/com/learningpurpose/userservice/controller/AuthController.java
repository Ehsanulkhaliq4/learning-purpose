package com.learningpurpose.userservice.controller;

import com.learningpurpose.userservice.dto.AuthRequest;
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

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping(value = "/forgot-password", params = "email")
    public ResponseEntity<Map<String, String>> forgotPasswordParam(@RequestParam("email") String email) {
        String cleanEmail = email.replace("\"", "").trim();
        authService.requestPasswordResetOtp(cleanEmail);
        return ResponseEntity.ok(Map.of("message", "OTP dispatched successfully"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody OtpVerificationRequest request) {
        authService.resetPasswordWithOtp(request);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }
}
