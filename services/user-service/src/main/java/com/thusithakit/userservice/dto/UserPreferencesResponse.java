package com.thusithakit.userservice.dto;

import java.time.Instant;
import java.util.UUID;

public record UserPreferencesResponse(
    UUID id,
    String locale,
    String timezone,
    String currency,
    boolean emailNotificationsEnabled,
    boolean pushNotificationsEnabled,
    boolean inAppNotificationsEnabled,
    Integer[] warrantyReminderDays,
    Instant createdAt,
    Instant updatedAt
) {
}