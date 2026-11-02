package com.learningpurpose.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learningpurpose.userservice.dto.CreateActivityRequest;
import com.learningpurpose.userservice.dto.DashboardResponseDto;
import com.learningpurpose.userservice.dto.DashboardResponseDto.ActivityDto;
import com.learningpurpose.userservice.dto.DashboardResponseDto.DailyBarDto;
import com.learningpurpose.userservice.dto.DashboardResponseDto.ResumeItemDto;
import com.learningpurpose.userservice.dto.LogStudyTimeRequest;
import com.learningpurpose.userservice.dto.UpdateResumeItemRequest;
import com.learningpurpose.userservice.exception.GlobalExceptionHandler;
import com.learningpurpose.userservice.exception.ResourceNotFoundException;
import com.learningpurpose.userservice.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DashboardControllerTest {

    private MockMvc mockMvc;
    private DashboardService dashboardService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        dashboardService = mock(DashboardService.class);
        DashboardController controller = new DashboardController(dashboardService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }

    @Test
    void getUserDashboard_Returns200WithNoCacheHeaders() throws Exception {
        DashboardResponseDto dto = DashboardResponseDto.builder()
                .learningStreak(DashboardResponseDto.MetricCardDto.builder().mainValue("5").unit("days").build())
                .build();
        when(dashboardService.resolveUserId("1")).thenReturn(1L);
        when(dashboardService.getUserDashboard(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/users/dashboard/1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store, must-revalidate, private"))
                .andExpect(header().string("Pragma", "no-cache"))
                .andExpect(header().string("Expires", "0"))
                .andExpect(jsonPath("$.learningStreak.mainValue").value("5"));
    }

    @Test
    void getUserDashboard_NotFound_Returns404() throws Exception {
        when(dashboardService.resolveUserId("999"))
                .thenThrow(new ResourceNotFoundException("User not found with id: 999"));

        mockMvc.perform(get("/api/v1/users/dashboard/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found with id: 999"));
    }

    @Test
    void getMyDashboard_WithAuth_Returns200() throws Exception {
        DashboardResponseDto dto = DashboardResponseDto.builder().build();
        when(dashboardService.getDashboardForUser("testuser")).thenReturn(dto);

        UsernamePasswordAuthenticationToken principal = new UsernamePasswordAuthenticationToken(
                "testuser", null, List.of(new SimpleGrantedAuthority("ROLE_NORMAL")));

        mockMvc.perform(get("/api/v1/users/dashboard/me").principal(principal))
                .andExpect(status().isOk())
                .andExpect(header().string("Pragma", "no-cache"));
    }

    @Test
    void getMyDashboard_WithoutAuth_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/users/dashboard/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logActivity_Returns201Created() throws Exception {
        ActivityDto activity = ActivityDto.builder()
                .id("10")
                .title("Watched Video")
                .subtitle("Spring Security Course")
                .timeAgo("Just now")
                .statusColor("blue")
                .build();
        when(dashboardService.resolveUserId("1")).thenReturn(1L);
        when(dashboardService.logUserActivity(eq(1L), any(CreateActivityRequest.class))).thenReturn(activity);

        CreateActivityRequest request = CreateActivityRequest.builder()
                .title("Watched Video")
                .subtitle("Spring Security Course")
                .actionType("MEDIA_WATCHED")
                .statusColor("blue")
                .build();

        mockMvc.perform(post("/api/v1/users/dashboard/1/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("10"))
                .andExpect(jsonPath("$.title").value("Watched Video"));
    }

    @Test
    void logStudyRhythm_Returns201Created() throws Exception {
        DailyBarDto bar = DailyBarDto.builder()
                .dayOfWeek("M")
                .hours(new BigDecimal("2.00"))
                .build();
        when(dashboardService.resolveUserId("1")).thenReturn(1L);
        when(dashboardService.logStudyTime(eq(1L), any(LogStudyTimeRequest.class))).thenReturn(bar);

        LogStudyTimeRequest request = LogStudyTimeRequest.builder()
                .hours(new BigDecimal("2.00"))
                .studyDate(LocalDate.now())
                .build();

        mockMvc.perform(post("/api/v1/users/dashboard/1/study-rhythm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dayOfWeek").value("M"))
                .andExpect(jsonPath("$.hours").value(2.00));
    }

    @Test
    void updateResumeItem_Returns201Created() throws Exception {
        ResumeItemDto item = ResumeItemDto.builder()
                .type("QUIZ")
                .title("Docker Essentials")
                .progressPercentage(50)
                .status("IN PROGRESS")
                .actionText("Resume")
                .actionUrl("/quizzes/10")
                .build();
        when(dashboardService.resolveUserId("1")).thenReturn(1L);
        when(dashboardService.saveOrUpdateResumeItem(eq(1L), any(UpdateResumeItemRequest.class))).thenReturn(item);

        UpdateResumeItemRequest request = UpdateResumeItemRequest.builder()
                .itemType("QUIZ")
                .referenceId(10L)
                .title("Docker Essentials")
                .progressPercentage(50)
                .build();

        mockMvc.perform(post("/api/v1/users/dashboard/1/resume-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Docker Essentials"))
                .andExpect(jsonPath("$.progressPercentage").value(50));
    }
}
