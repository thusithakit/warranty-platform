package com.thusithakit.userservice.service;

import com.thusithakit.userservice.dto.UpdateUserPreferencesRequest;
import com.thusithakit.userservice.dto.UserPreferencesResponse;
import com.thusithakit.userservice.entity.UserPreferences;
import com.thusithakit.userservice.repository.UserPreferencesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Arrays;

@Service
public class UserPreferencesService {

  private final UserProfileService userProfileService;
  private final UserPreferencesRepository userPreferencesRepository;

  public UserPreferencesService(
      UserProfileService userProfileService,
      UserPreferencesRepository userPreferencesRepository
  ) {
    this.userProfileService = userProfileService;
    this.userPreferencesRepository = userPreferencesRepository;
  }

  @Transactional
  public UserPreferencesResponse getPreferences(
      String userId,
      String email,
      String firstName,
      String lastName
  ) {
    // Ensure the profile and default preferences exist.
    userProfileService.getOrCreateProfile(
        userId,
        email,
        firstName,
        lastName
    );

    UserPreferences preferences = findPreferences(userId);

    return toResponse(preferences);
  }

  @Transactional
  public UserPreferencesResponse updatePreferences(
      String userId,
      String email,
      String firstName,
      String lastName,
      UpdateUserPreferencesRequest request
  ) {
    userProfileService.getOrCreateProfile(
        userId,
        email,
        firstName,
        lastName
    );

    validateTimezone(request.timezone());
    validateReminderDays(request.warrantyReminderDays());

    UserPreferences preferences = findPreferences(userId);

    preferences.setLocale(request.locale().trim());
    preferences.setTimezone(request.timezone().trim());
    preferences.setCurrency(request.currency());
    preferences.setEmailNotificationsEnabled(
        request.emailNotificationsEnabled()
    );
    preferences.setPushNotificationsEnabled(
        request.pushNotificationsEnabled()
    );
    preferences.setInAppNotificationsEnabled(
        request.inAppNotificationsEnabled()
    );
    preferences.setWarrantyReminderDays(
        request.warrantyReminderDays().clone()
    );

    return toResponse(preferences);
  }

  private UserPreferences findPreferences(String userId) {
    return userPreferencesRepository
        .findByKeycloakUserId(userId)
        .orElseThrow(() ->
            new IllegalStateException("User preferences not found"));
  }

  private void validateTimezone(String timezone) {
    try {
      ZoneId.of(timezone);
    } catch (DateTimeException exception) {
      throw new IllegalArgumentException("Invalid timezone: " + timezone);
    }
  }

  private void validateReminderDays(Integer[] days) {
    if (days == null || days.length == 0) {
      throw new IllegalArgumentException(
          "At least one reminder interval is required"
      );
    }

    boolean invalid = Arrays.stream(days)
        .anyMatch(day -> day == null || day < 1 || day > 365);

    if (invalid) {
      throw new IllegalArgumentException(
          "Reminder intervals must be between 1 and 365 days"
      );
    }

    long uniqueCount = Arrays.stream(days).distinct().count();

    if (uniqueCount != days.length) {
      throw new IllegalArgumentException(
          "Reminder intervals must not contain duplicates"
      );
    }
  }

  private UserPreferencesResponse toResponse(UserPreferences preferences) {
    return new UserPreferencesResponse(
        preferences.getId(),
        preferences.getLocale(),
        preferences.getTimezone(),
        preferences.getCurrency(),
        preferences.isEmailNotificationsEnabled(),
        preferences.isPushNotificationsEnabled(),
        preferences.isInAppNotificationsEnabled(),
        preferences.getWarrantyReminderDays(),
        preferences.getCreatedAt(),
        preferences.getUpdatedAt()
    );
  }
}