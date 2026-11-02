package com.learningpurpose.userservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogStudyTimeRequest {

    @NotNull(message = "Hours spent is required")
    @DecimalMin(value = "0.01", message = "Hours spent must be greater than 0")
    private BigDecimal hours;

    private LocalDate studyDate;
}
