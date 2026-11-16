package com.learningpurpose.userservice.dto;

import com.learningpurpose.userservice.model.User;

import java.time.Instant;

public record AdminManagedUserDto(
        Long id,
        String username,
        String firstName,
        String lastName,
        String email,
        String phone,
        boolean enabled,
        Instant createdAt,
        Instant deactivatedUntil
) {
    public static AdminManagedUserDto from(User user) {
        return new AdminManagedUserDto(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.isEnabled(),
                user.getCreatedAt(),
                user.getDeactivatedUntil()
        );
    }
}
