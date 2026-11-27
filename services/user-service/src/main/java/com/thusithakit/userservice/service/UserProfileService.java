package com.thusithakit.userservice.service;

import com.thusithakit.userservice.dto.UpdateUserProfileRequest;
import com.thusithakit.userservice.dto.UserProfileResponse;
import com.thusithakit.userservice.entity.UserProfile;
import com.thusithakit.userservice.repository.UserPreferencesRepository;
import com.thusithakit.userservice.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

  private final UserProfileRepository userProfileRepository;
  private final UserPreferencesRepository userPreferencesRepository;

  public UserProfileService(
      UserProfileRepository userProfileRepository,
      UserPreferencesRepository userPreferencesRepository
  ) {
    this.userProfileRepository = userProfileRepository;
    this.userPreferencesRepository = userPreferencesRepository;
  }

  @Transactional
  public UserProfileResponse getOrCreateProfile(
      String userId,
      String email,
      String firstName,
      String lastName
  ) {
    if (userId == null || userId.isBlank()) {
      throw new IllegalArgumentException("Authenticated user ID is required");
    }

    userProfileRepository.insertIfAbsent(
        userId,
        email,
        firstName,
        lastName
    );

    userPreferencesRepository.insertIfAbsent(userId);

    userProfileRepository.synchronizeIdentity(
        userId,
        email,
        firstName,
        lastName
    );

    UserProfile profile = userProfileRepository
        .findByKeycloakUserId(userId)
        .orElseThrow(() ->
            new IllegalStateException("Unable to provision user profile"));

    return toResponse(profile);
  }

  @Transactional
  public UserProfileResponse updateProfile(
      String userId,
      String email,
      String firstName,
      String lastName,
      UpdateUserProfileRequest request
  ) {
    // Provision the profile if necessary and synchronize Keycloak claims.
    getOrCreateProfile(userId, email, firstName, lastName);

    UserProfile profile = userProfileRepository
        .findByKeycloakUserId(userId)
        .orElseThrow(() ->
            new IllegalStateException("User profile not found"));

    profile.setPhoneNumber(normalize(request.phoneNumber()));
    profile.setAvatarUrl(normalize(request.avatarUrl()));

    return toResponse(profile);
  }

  private String normalize(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    return value.trim();
  }

  private UserProfileResponse toResponse(UserProfile profile) {
    return new UserProfileResponse(
        profile.getId(),
        profile.getEmail(),
        profile.getFirstName(),
        profile.getLastName(),
        profile.getPhoneNumber(),
        profile.getAvatarUrl(),
        profile.getCreatedAt(),
        profile.getUpdatedAt()
    );
  }
}