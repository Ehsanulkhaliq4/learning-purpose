package com.learningpurpose.userservice.service.impl;

import com.learningpurpose.userservice.dto.UpdateUserProfileRequest;
import com.learningpurpose.userservice.dto.UserProfileDto;
import com.learningpurpose.userservice.dto.UserProfileDto.QuizActivityDto;
import com.learningpurpose.userservice.exception.UserAlreadyExistsException;
import com.learningpurpose.userservice.model.User;
import com.learningpurpose.userservice.model.UserActivityLog;
import com.learningpurpose.userservice.model.UserLearningMetrics;
import com.learningpurpose.userservice.repository.UserActivityLogRepository;
import com.learningpurpose.userservice.repository.UserLearningMetricsRepository;
import com.learningpurpose.userservice.repository.UserRepository;
import com.learningpurpose.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserActivityLogRepository activityRepository;
    private final UserLearningMetricsRepository metricsRepository;

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
    }

    @Override
    public List<User> searchUsersByUsername(String query) {
        return userRepository.findTop20ByUsernameContainingIgnoreCaseOrderByUsernameAsc(query);
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileDto getMyProfile(String username) {
        return toProfile(getAccount(username));
    }

    @Override
    @Transactional
    public UserProfileDto updateMyProfile(String username, UpdateUserProfileRequest request) {
        User user = getAccount(username);
        String email = request.email().trim().toLowerCase();
        userRepository.findByEmail(email)
                .filter(existing -> !existing.getId().equals(user.getId()))
                .ifPresent(existing -> {
                    throw new UserAlreadyExistsException("Email already in use: " + email);
                });
        validateProfileImage(request.profile());

        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName() == null ? null : request.lastName().trim());
        user.setEmail(email);
        user.setPhone(request.phone() == null || request.phone().isBlank() ? null : request.phone().trim());
        user.setProfile(request.profile() == null || request.profile().isBlank() ? null : request.profile());
        return toProfile(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deactivateMyAccount(String username) {
        User user = getAccount(username);
        user.setEnabled(false);
        userRepository.save(user);
    }

    private User getAccount(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    private UserProfileDto toProfile(User user) {
        UserLearningMetrics metrics = metricsRepository.findByUserId(user.getId()).orElse(null);
        List<QuizActivityDto> attempts = activityRepository
                .findTop10ByUserIdAndActionTypeOrderByCreatedAtDesc(user.getId(), "EXAM_SUBMITTED")
                .stream()
                .map(this::toQuizActivity)
                .toList();

        return new UserProfileDto(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getProfile(),
                user.isEnabled(),
                user.getCreatedAt(),
                activityRepository.countByUserIdAndActionType(user.getId(), "EXAM_SUBMITTED"),
                metrics == null ? 0 : metrics.getCompletedLessons(),
                metrics == null ? BigDecimal.ZERO : metrics.getAssessmentScore(),
                metrics == null ? BigDecimal.ZERO : metrics.getStudyTimeHours(),
                metrics == null ? 0 : metrics.getCurrentStreakDays(),
                attempts
        );
    }

    private QuizActivityDto toQuizActivity(UserActivityLog activity) {
        return new QuizActivityDto(activity.getTitle(), activity.getSubtitle(), activity.getCreatedAt());
    }

    private void validateProfileImage(String profile) {
        if (profile == null || !profile.startsWith("data:")) {
            return;
        }
        String[] parts = profile.split(",", 2);
        if (parts.length != 2 || !parts[0].matches("data:image/(jpeg|png|webp);base64")) {
            throw new IllegalArgumentException("Profile image must be a PNG, JPEG, or WebP image.");
        }
        byte[] imageBytes;
        try {
            imageBytes = Base64.getDecoder().decode(parts[1]);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Profile image data is invalid.", ex);
        }
        if (imageBytes.length > 512_000) {
            throw new IllegalArgumentException("Profile image must be 512 KB or smaller after processing.");
        }
    }
}
