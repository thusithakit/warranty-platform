package com.thusithakit.userservice.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(

    @Pattern(
        regexp = "^$|^\\+?[0-9() .-]{7,30}$",
        message = "Phone number must be valid"
    )
    String phoneNumber,

    @Size(max = 2048, message = "Avatar URL cannot exceed 2048 characters")
    @Pattern(
        regexp = "^$|^https?://.*$",
        message = "Avatar URL must use HTTP or HTTPS"
    )
    String avatarUrl
) {
}