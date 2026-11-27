package com.thusithakit.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfile {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "keycloak_user_id", nullable = false, unique = true, length = 255)
  private String keycloakUserId;

  @Column(length = 320)
  private String email;

  @Column(name = "first_name", length = 100)
  private String firstName;

  @Column(name = "last_name", length = 100)
  private String lastName;

  @Column(name = "phone_number", length = 30)
  private String phoneNumber;

  @Column(name = "avatar_url")
  private String avatarUrl;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Version
  @Column(nullable = false)
  private Long version;

  public UserProfile(String keycloakUserId) {
    this.keycloakUserId = keycloakUserId;
  }

  @PrePersist
  protected void onCreate() {
    Instant now = Instant.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = Instant.now();
  }
}