package com.thusithakit.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserPreferencesRequest(

    @NotBlank(message = "Locale is required")
    @Size(max = 20)
    @Pattern(
        regexp = "^[a-zA-Z]{2,3}(-[a-zA-Z0-9]{2,8})*$",
        message = "Invalid locale format"
    )
    String locale,

    @NotBlank(message = "Timezone is required")
    @Size(max = 60)
    String timezone,

    @NotBlank(message = "Currency is required")
    @Pattern(
        regexp = "^[A-Z]{3}$",
        message = "Currency must be a three-letter uppercase code"
    )
    String currency,

    boolean emailNotificationsEnabled,

    boolean pushNotificationsEnabled,

    boolean inAppNotificationsEnabled,

    @NotEmpty(message = "At least one warranty reminder interval is required")
    @Size(max = 10, message = "Maximum 10 reminder intervals are allowed")
    Integer[] warrantyReminderDays
) {
}