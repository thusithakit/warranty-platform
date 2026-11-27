package com.thusithakit.userservice.controller;

import com.thusithakit.userservice.dto.UpdateUserPreferencesRequest;
import com.thusithakit.userservice.dto.UpdateUserProfileRequest;
import com.thusithakit.userservice.dto.UserPreferencesResponse;
import com.thusithakit.userservice.dto.UserProfileResponse;
import com.thusithakit.userservice.service.UserPreferencesService;
import com.thusithakit.userservice.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@RequestMapping("/api/v1/users")
public class UserProfileController {

  private final UserProfileService userProfileService;
  private final UserPreferencesService userPreferencesService;

  public UserProfileController(UserProfileService userProfileService, UserPreferencesService userPreferencesService) {
    this.userProfileService = userProfileService;
    this.userPreferencesService = userPreferencesService;
  }

  @GetMapping("/me")
  public UserProfileResponse getMyProfile(
      @AuthenticationPrincipal Jwt jwt
  ) {
    return userProfileService.getOrCreateProfile(
        jwt.getSubject(),
        jwt.getClaimAsString("email"),
        jwt.getClaimAsString("given_name"),
        jwt.getClaimAsString("family_name")
    );
  }

  @PutMapping("/me")
  public UserProfileResponse updateMyProfile(
      @AuthenticationPrincipal Jwt jwt,
      @Valid @RequestBody UpdateUserProfileRequest request
  ) {
    return userProfileService.updateProfile(
        jwt.getSubject(),
        jwt.getClaimAsString("email"),
        jwt.getClaimAsString("given_name"),
        jwt.getClaimAsString("family_name"),
        request
    );
  }

  @GetMapping("/me/preferences")
  public UserPreferencesResponse getMyPreferences(
      @AuthenticationPrincipal Jwt jwt
  ) {
    return userPreferencesService.getPreferences(
        jwt.getSubject(),
        jwt.getClaimAsString("email"),
        jwt.getClaimAsString("given_name"),
        jwt.getClaimAsString("family_name")
    );
  }

  @PutMapping("/me/preferences")
  public UserPreferencesResponse updateMyPreferences(
      @AuthenticationPrincipal Jwt jwt,
      @Valid @RequestBody UpdateUserPreferencesRequest request
  ) {
    return userPreferencesService.updatePreferences(
        jwt.getSubject(),
        jwt.getClaimAsString("email"),
        jwt.getClaimAsString("given_name"),
        jwt.getClaimAsString("family_name"),
        request
    );
  }
}