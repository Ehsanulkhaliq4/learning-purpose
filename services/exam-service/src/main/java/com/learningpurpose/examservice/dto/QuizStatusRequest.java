package com.learningpurpose.examservice.dto;

import jakarta.validation.constraints.NotNull;

public record QuizStatusRequest(@NotNull Boolean active) {
}
