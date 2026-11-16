package com.learningpurpose.userservice.service;

import com.learningpurpose.userservice.config.JwtService;
import com.learningpurpose.userservice.dto.AuthRequest;
import com.learningpurpose.userservice.exception.AccountDeactivatedException;
import com.learningpurpose.userservice.model.User;
import com.learningpurpose.userservice.repository.RoleRepository;
import com.learningpurpose.userservice.repository.SaveOtpRepository;
import com.learningpurpose.userservice.repository.UserRepository;
import com.learningpurpose.userservice.service.impl.AuthServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private SaveOtpRepository saveOtpRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private ObjectMapper objectMapper;
    @Mock private org.springframework.kafka.core.KafkaTemplate<String, String> kafkaTemplate;
    @InjectMocks private AuthServiceImpl authService;

    @Test
    void loginExplainsWhenAccountIsStillDeactivated() {
        Instant reactivationTime = Instant.now().plusSeconds(86_400);
        User user = User.builder()
                .username("learner")
                .enabled(false)
                .deactivatedUntil(reactivationTime)
                .build();
        when(userRepository.findByUsername("learner")).thenReturn(Optional.of(user));

        AccountDeactivatedException exception = assertThrows(
                AccountDeactivatedException.class,
                () -> authService.login(loginRequest())
        );

        assertTrue(exception.getMessage().contains(reactivationTime.toString()));
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void loginReactivatesExpiredAccountBeforeAuthenticating() {
        User user = User.builder()
                .username("learner")
                .email("learner@example.com")
                .enabled(false)
                .deactivatedUntil(Instant.now().minusSeconds(1))
                .build();
        when(userRepository.findByUsername("learner")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("token");

        authService.login(loginRequest());

        assertTrue(user.isEnabled());
        assertNull(user.getDeactivatedUntil());
        verify(userRepository).save(user);
        verify(authenticationManager).authenticate(any());
    }

    private AuthRequest loginRequest() {
        AuthRequest request = new AuthRequest();
        request.setUsername("learner");
        request.setPassword("password");
        return request;
    }
}
