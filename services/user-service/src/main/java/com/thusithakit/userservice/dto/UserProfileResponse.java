package com.thusithakit.userservice.dto;

import java.time.Instant;
import java.util.UUID;

public record UserProfileResponse(
    UUID id,
    String email,
    String firstName,
    String lastName,
    String phoneNumber,
    String avatarUrl,
    Instant createdAt,
    Instant updatedAt
) {
}