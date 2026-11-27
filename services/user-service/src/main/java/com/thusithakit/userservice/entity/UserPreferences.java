package com.thusithakit.userservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_preferences")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserPreferences {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "keycloak_user_id", nullable = false, unique = true)
  private String keycloakUserId;

  @Column(nullable = false, length = 20)
  private String locale = "en-LK";

  @Column(nullable = false, length = 60)
  private String timezone = "Asia/Colombo";

  @Column(nullable = false, length = 3)
  private String currency = "LKR";

  @Column(name = "email_notifications_enabled", nullable = false)
  private boolean emailNotificationsEnabled = true;

  @Column(name = "push_notifications_enabled", nullable = false)
  private boolean pushNotificationsEnabled = true;

  @Column(name = "in_app_notifications_enabled", nullable = false)
  private boolean inAppNotificationsEnabled = true;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(name = "warranty_reminder_days", nullable = false, columnDefinition = "integer[]")
  private Integer[] warrantyReminderDays = {30, 7, 1};

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public UserPreferences(String keycloakUserId) {
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