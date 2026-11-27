package com.thusithakit.userservice.repository;

import com.thusithakit.userservice.entity.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserPreferencesRepository extends JpaRepository<UserPreferences, UUID> {

  Optional<UserPreferences> findByKeycloakUserId(String keycloakUserId);

  @Modifying
  @Query(value = """
        INSERT INTO user_preferences (keycloak_user_id)
        VALUES (:userId)
        ON CONFLICT (keycloak_user_id) DO NOTHING
        """, nativeQuery = true)
  int insertIfAbsent(@Param("userId") String userId);
}