package com.learningpurpose.userservice.service;

import com.learningpurpose.userservice.dto.DashboardResponseDto;

public interface DashboardService {
    DashboardResponseDto getUserDashboard(Long userId);
}
