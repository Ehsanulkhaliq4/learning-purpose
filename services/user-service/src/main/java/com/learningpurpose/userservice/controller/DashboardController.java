package com.learningpurpose.userservice.controller;

import com.learningpurpose.userservice.dto.DashboardResponseDto;
import com.learningpurpose.userservice.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/{userId}")
    public ResponseEntity<DashboardResponseDto> getUserDashboard(@PathVariable Long userId) {
        DashboardResponseDto dashboard = dashboardService.getUserDashboard(userId);
        return ResponseEntity.ok(dashboard);
    }
}
