package com.learningpurpose.userservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learningpurpose.userservice.config.JwtService;
import com.learningpurpose.userservice.dto.AuthRequest;
import com.learningpurpose.userservice.dto.AuthResponse;
import com.learningpurpose.userservice.dto.OtpVerificationRequest;
import com.learningpurpose.userservice.dto.RegisterRequest;
import com.learningpurpose.userservice.event.OtpRequestedEvent;
import com.learningpurpose.userservice.event.UserRegisteredEvent;
import com.learningpurpose.userservice.exception.InvalidOtpException;
import com.learningpurpose.userservice.exception.UserAlreadyExistsException;
import com.learningpurpose.userservice.model.Role;
import com.learningpurpose.userservice.model.SaveOtp;
import com.learningpurpose.userservice.model.User;
import com.learningpurpose.userservice.repository.RoleRepository;
import com.learningpurpose.userservice.repository.SaveOtpRepository;
import com.learningpurpose.userservice.repository.UserRepository;
import com.learningpurpose.userservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SaveOtpRepository saveOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String , String> kafkaTemplate;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String TOPIC_USER_REGISTERED = "user-registered-topic";
    private static final String TOPIC_OTP_REQUESTED = "otp-requested-topic";

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if(userRepository.existsByUsername(request.getUsername())){
            throw new UserAlreadyExistsException("Username already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())){
            throw new UserAlreadyExistsException("Email already in use: " + request.getEmail());
        }
        Role defaultRole = roleRepository.findByRoleName("ROLE_NORMAL")
                .orElseGet(() -> roleRepository.save(Role.builder().roleName("ROLE_NORMAL").build()));
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .enabled(true)
                .roles(Set.of(defaultRole))
                .build();

        User savedUser = userRepository.save(user);
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .registeredAt(Instant.now())
                .build();
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC_USER_REGISTERED,savedUser.getEmail(),payload);
        } catch (Exception ex) {
            log.error("Failed to serialize and dispatch OTP event", ex);
        }

        String jwtToken = jwtService.generateToken(savedUser);
        return AuthResponse.builder()
                .token(jwtToken)
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .roles(savedUser.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet()))
                .build();
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(),request.getPassword()));
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        String jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(jwtToken)
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet()))
                .build();
    }

    @Override
    @Transactional
    public void requestPasswordResetOtp(String email) {
        String normalizedEmail = email.replace("\"", "").trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + normalizedEmail));

        // Generate a 6-digit numeric OTP (%06d instead of %060)
        String otp = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
        Instant expiresAt = Instant.now().plus(Duration.ofMinutes(10));

        // Invalidate any active, unused OTPs for this email before issuing a new one
        saveOtpRepository.deleteByEmailAndUsedFalse(user.getEmail());

        SaveOtp saveOtp = SaveOtp.builder()
                .email(user.getEmail())
                .otp(otp)
                .expiresAt(expiresAt)
                .used(false)
                .build();
        saveOtpRepository.save(saveOtp);

        OtpRequestedEvent otpEvent = OtpRequestedEvent.builder()
                .email(user.getEmail())
                .otp(otp)
                .expiresAt(expiresAt)
                .build();
        try {
            String payload = objectMapper.writeValueAsString(otpEvent);
            kafkaTemplate.send(TOPIC_OTP_REQUESTED, user.getEmail(), payload)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Dispatched OTP event for [{}] to partition [{}]",
                                    user.getEmail(), result.getRecordMetadata().partition());
                        } else {
                            log.error("Failed to publish OTP event for [{}]", user.getEmail(), ex);
                        }
                    });
        }catch (Exception ex) {
            log.error("Failed to serialize and dispatch OTP event", ex);
        }
    }

    @Override
    @Transactional
    public void resetPasswordWithOtp(OtpVerificationRequest request) {
        SaveOtp validOtp = saveOtpRepository.findTopByEmailAndOtpAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(request.getEmail(), request.getOtp(), Instant.now())
                .orElseThrow(() -> new InvalidOtpException("Invalid or expired OTP"));
        validOtp.setUsed(true);
        saveOtpRepository.save(validOtp);
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
