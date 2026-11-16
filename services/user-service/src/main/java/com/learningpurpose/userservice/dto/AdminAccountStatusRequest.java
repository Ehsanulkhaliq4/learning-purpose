package com.learningpurpose.userservice.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record AdminAccountStatusRequest(
        @NotNull Boolean active,
        DeactivationPeriod period,
        Instant reactivatesAt
) {
    public enum DeactivationPeriod {
        FIFTEEN_DAYS,
        THIRTY_DAYS,
        PERMANENT,
        CUSTOM
    }
}
