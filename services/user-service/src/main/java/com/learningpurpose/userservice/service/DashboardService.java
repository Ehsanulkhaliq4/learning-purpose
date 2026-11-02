package com.learningpurpose.userservice.service;

import com.learningpurpose.userservice.dto.CreateActivityRequest;
import com.learningpurpose.userservice.dto.DashboardResponseDto;
import com.learningpurpose.userservice.dto.LogStudyTimeRequest;
import com.learningpurpose.userservice.dto.UpdateResumeItemRequest;

public interface DashboardService {
    DashboardResponseDto getUserDashboard(Long userId);
    DashboardResponseDto getDashboardForUser(String usernameOrEmail);
    Long resolveUserId(String identifier);
    DashboardResponseDto.ActivityDto logUserActivity(Long userId, CreateActivityRequest request);
    DashboardResponseDto.DailyBarDto logStudyTime(Long userId, LogStudyTimeRequest request);
    DashboardResponseDto.ResumeItemDto saveOrUpdateResumeItem(Long userId, UpdateResumeItemRequest request);
}
