package com.learningpurpose.userservice.controller;

import com.learningpurpose.userservice.dto.CreateActivityRequest;
import com.learningpurpose.userservice.dto.DashboardResponseDto;
import com.learningpurpose.userservice.dto.DashboardResponseDto.ActivityDto;
import com.learningpurpose.userservice.dto.DashboardResponseDto.DailyBarDto;
import com.learningpurpose.userservice.dto.DashboardResponseDto.ResumeItemDto;
import com.learningpurpose.userservice.dto.LogStudyTimeRequest;
import com.learningpurpose.userservice.dto.UpdateResumeItemRequest;
import com.learningpurpose.userservice.service.DashboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/me")
    public ResponseEntity<DashboardResponseDto> getMyDashboard(
            @RequestHeader(value = "X-Authenticated-User", required = false) String gatewayUser,
            Authentication authentication) {
        String username = resolveAuthUsername(authentication, gatewayUser);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        DashboardResponseDto dashboard = dashboardService.getDashboardForUser(username);
        return createNoCacheResponse(dashboard);
    }

    @GetMapping("/{identifier}")
    public ResponseEntity<DashboardResponseDto> getUserDashboard(
            @PathVariable String identifier,
            @RequestHeader(value = "X-Authenticated-User", required = false) String gatewayUser,
            Authentication authentication) {
        if ("me".equalsIgnoreCase(identifier)) {
            return getMyDashboard(gatewayUser, authentication);
        }
        Long resolvedId = dashboardService.resolveUserId(identifier);
        DashboardResponseDto dashboard = dashboardService.getUserDashboard(resolvedId);
        return createNoCacheResponse(dashboard);
    }

    @PostMapping("/{identifier}/activities")
    public ResponseEntity<ActivityDto> logActivity(
            @PathVariable String identifier,
            @RequestHeader(value = "X-Authenticated-User", required = false) String gatewayUser,
            Authentication authentication,
            @Valid @RequestBody CreateActivityRequest request) {
        Long resolvedId = resolveTargetUserId(identifier, gatewayUser, authentication);
        ActivityDto activity = dashboardService.logUserActivity(resolvedId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(activity);
    }

    @PostMapping("/{identifier}/study-rhythm")
    public ResponseEntity<DailyBarDto> logStudyRhythm(
            @PathVariable String identifier,
            @RequestHeader(value = "X-Authenticated-User", required = false) String gatewayUser,
            Authentication authentication,
            @Valid @RequestBody LogStudyTimeRequest request) {
        Long resolvedId = resolveTargetUserId(identifier, gatewayUser, authentication);
        DailyBarDto bar = dashboardService.logStudyTime(resolvedId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(bar);
    }

    @PostMapping("/{identifier}/resume-items")
    public ResponseEntity<ResumeItemDto> updateResumeItem(
            @PathVariable String identifier,
            @RequestHeader(value = "X-Authenticated-User", required = false) String gatewayUser,
            Authentication authentication,
            @Valid @RequestBody UpdateResumeItemRequest request) {
        Long resolvedId = resolveTargetUserId(identifier, gatewayUser, authentication);
        ResumeItemDto item = dashboardService.saveOrUpdateResumeItem(resolvedId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    private Long resolveTargetUserId(String identifier, String gatewayUser, Authentication authentication) {
        if ("me".equalsIgnoreCase(identifier)) {
            String username = resolveAuthUsername(authentication, gatewayUser);
            if (username == null) {
                throw new IllegalArgumentException("Authentication required to operate on 'me'");
            }
            return dashboardService.resolveUserId(username);
        }
        return dashboardService.resolveUserId(identifier);
    }

    private String resolveAuthUsername(Authentication authentication, String gatewayUser) {
        if (authentication != null && authentication.getName() != null && !authentication.getName().isBlank()) {
            return authentication.getName();
        }
        if (gatewayUser != null && !gatewayUser.isBlank()) {
            return gatewayUser.trim();
        }
        return null;
    }

    private ResponseEntity<DashboardResponseDto> createNoCacheResponse(DashboardResponseDto body) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore().mustRevalidate().cachePrivate())
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .body(body);
    }
}
