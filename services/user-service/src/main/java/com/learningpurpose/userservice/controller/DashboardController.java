package com.learningpurpose.userservice.controller;

import com.learningpurpose.userservice.dto.DashboardResponseDto;
import com.learningpurpose.userservice.model.User;
import com.learningpurpose.userservice.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponseDto> getDashboard(@AuthenticationPrincipal User principal) {
        Long userId = (principal != null) ? principal.getId() : 1L;
        return ResponseEntity.ok(dashboardService.getUserDashboard(userId));
    }
}
