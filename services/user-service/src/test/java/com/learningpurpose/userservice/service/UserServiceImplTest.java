package com.learningpurpose.userservice.service;

import com.learningpurpose.userservice.dto.UpdateUserProfileRequest;
import com.learningpurpose.userservice.dto.AdminAccountStatusRequest;
import com.learningpurpose.userservice.exception.UserAlreadyExistsException;
import com.learningpurpose.userservice.model.Role;
import com.learningpurpose.userservice.model.User;
import com.learningpurpose.userservice.model.UserActivityLog;
import com.learningpurpose.userservice.model.UserLearningMetrics;
import com.learningpurpose.userservice.repository.UserActivityLogRepository;
import com.learningpurpose.userservice.repository.UserLearningMetricsRepository;
import com.learningpurpose.userservice.repository.UserRepository;
import com.learningpurpose.userservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserActivityLogRepository activityRepository;
    @Mock
    private UserLearningMetricsRepository metricsRepository;
    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(12L)
                .username("learner")
                .firstName("Ada")
                .lastName("Lovelace")
                .email("ada@example.com")
                .enabled(true)
                .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
                .build();
    }

    @Test
    void getMyProfileIncludesLearningMetricsAndRecentQuizActivity() {
        when(userRepository.findByUsername("learner")).thenReturn(Optional.of(user));
        UserLearningMetrics metrics = UserLearningMetrics.builder()
                .completedLessons(4)
                .assessmentScore(new BigDecimal("92.50"))
                .studyTimeHours(new BigDecimal("8.50"))
                .currentStreakDays(3)
                .build();
        UserActivityLog attempt = UserActivityLog.builder()
                .title("Completed Algebra")
                .subtitle("Scored 92.5% - Passed")
                .createdAt(Instant.parse("2025-02-01T00:00:00Z"))
                .build();
        when(metricsRepository.findByUserId(12L)).thenReturn(Optional.of(metrics));
        when(activityRepository.countByUserIdAndActionType(12L, "EXAM_SUBMITTED")).thenReturn(7L);
        when(activityRepository.findTop10ByUserIdAndActionTypeOrderByCreatedAtDesc(12L, "EXAM_SUBMITTED"))
                .thenReturn(List.of(attempt));

        var profile = userService.getMyProfile("learner");

        assertEquals(7, profile.quizAttempts());
        assertEquals(4, profile.completedLessons());
        assertEquals(new BigDecimal("92.50"), profile.bestScore());
        assertEquals("Completed Algebra", profile.recentQuizAttempts().getFirst().title());
    }

    @Test
    void updateMyProfilePersistsContactDetailsAndImage() {
        when(userRepository.findByUsername("learner")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("ada.new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(metricsRepository.findByUserId(12L)).thenReturn(Optional.empty());
        when(activityRepository.countByUserIdAndActionType(12L, "EXAM_SUBMITTED")).thenReturn(0L);
        when(activityRepository.findTop10ByUserIdAndActionTypeOrderByCreatedAtDesc(12L, "EXAM_SUBMITTED"))
                .thenReturn(List.of());

        var profile = userService.updateMyProfile("learner", new UpdateUserProfileRequest(
                "Ada", "Byron", " ADA.NEW@example.com ", "+1 555 010 2000",
                "data:image/jpeg;base64,YQ=="
        ));

        assertEquals("ada.new@example.com", profile.email());
        assertEquals("Byron", profile.lastName());
        assertEquals("+1 555 010 2000", profile.phone());
        assertEquals("data:image/jpeg;base64,YQ==", profile.profile());
        assertEquals("learner", profile.username());
    }

    @Test
    void updateMyProfileRejectsEmailUsedByAnotherAccount() {
        when(userRepository.findByUsername("learner")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("other@example.com"))
                .thenReturn(Optional.of(User.builder().id(13L).build()));

        assertThrows(UserAlreadyExistsException.class, () -> userService.updateMyProfile(
                "learner",
                new UpdateUserProfileRequest("Ada", "Lovelace", "other@example.com", "", null)
        ));
    }

    @Test
    void deactivateMyAccountDisablesRatherThanDeletesTheAccount() {
        when(userRepository.findByUsername("learner")).thenReturn(Optional.of(user));
        userService.deactivateMyAccount("learner");

        assertFalse(user.isEnabled());
        assertEquals(null, user.getDeactivatedUntil());
        verify(userRepository).save(user);
    }

    @Test
    void adminCanDeactivateAccountForFifteenDays() {
        when(userRepository.findById(12L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Instant before = Instant.now();

        var result = userService.updateManagedAccountStatus(
                12L,
                new AdminAccountStatusRequest(false, AdminAccountStatusRequest.DeactivationPeriod.FIFTEEN_DAYS, null)
        );

        assertFalse(result.enabled());
        assertTrue(result.deactivatedUntil().isAfter(before.plus(Duration.ofDays(14))));
        assertTrue(result.deactivatedUntil().isBefore(before.plus(Duration.ofDays(16))));
    }

    @Test
    void adminCanPermanentlyDeactivateAndManuallyReactivateAccount() {
        when(userRepository.findById(12L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var deactivated = userService.updateManagedAccountStatus(
                12L,
                new AdminAccountStatusRequest(false, AdminAccountStatusRequest.DeactivationPeriod.PERMANENT, null)
        );
        assertFalse(deactivated.enabled());
        assertEquals(null, deactivated.deactivatedUntil());

        var activated = userService.updateManagedAccountStatus(
                12L,
                new AdminAccountStatusRequest(true, null, null)
        );
        assertTrue(activated.enabled());
        assertEquals(null, activated.deactivatedUntil());
    }

    @Test
    void adminCanSetCustomReactivationTime() {
        Instant reactivationTime = Instant.now().plus(Duration.ofDays(5));
        when(userRepository.findById(12L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = userService.updateManagedAccountStatus(
                12L,
                new AdminAccountStatusRequest(false, AdminAccountStatusRequest.DeactivationPeriod.CUSTOM, reactivationTime)
        );

        assertEquals(reactivationTime, result.deactivatedUntil());
    }

    @Test
    void customReactivationTimeMustBeInTheFuture() {
        when(userRepository.findById(12L)).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> userService.updateManagedAccountStatus(
                12L,
                new AdminAccountStatusRequest(
                        false,
                        AdminAccountStatusRequest.DeactivationPeriod.CUSTOM,
                        Instant.now().minusSeconds(1)
                )
        ));
    }

    @Test
    void adminCannotManageAnotherAdminAccount() {
        User administrator = User.builder()
                .id(12L)
                .roles(java.util.Set.of(Role.builder().roleName("ROLE_ADMIN").build()))
                .build();
        when(userRepository.findById(12L)).thenReturn(Optional.of(administrator));

        assertThrows(IllegalArgumentException.class, () -> userService.updateManagedAccountStatus(
                12L,
                new AdminAccountStatusRequest(false, AdminAccountStatusRequest.DeactivationPeriod.PERMANENT, null)
        ));
    }
}
