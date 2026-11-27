package com.thusithakit.userservice.repository;

import com.thusithakit.userservice.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

  Optional<UserProfile> findByKeycloakUserId(String keycloakUserId);

  @Modifying
  @Query(value = """
        INSERT INTO user_profiles (keycloak_user_id, email, first_name, last_name)
        VALUES (:userId, :email, :firstName, :lastName)
        ON CONFLICT (keycloak_user_id) DO NOTHING
        """, nativeQuery = true)
  int insertIfAbsent(
      @Param("userId") String userId,
      @Param("email") String email,
      @Param("firstName") String firstName,
      @Param("lastName") String lastName
  );

  @Modifying
  @Query(value = """
    UPDATE user_profiles
    SET email = COALESCE(:email, email),
        first_name = COALESCE(:firstName, first_name),
        last_name = COALESCE(:lastName, last_name),
        updated_at = CURRENT_TIMESTAMP
    WHERE keycloak_user_id = :userId
      AND (
          (:email IS NOT NULL AND email IS DISTINCT FROM :email)
          OR (:firstName IS NOT NULL AND first_name IS DISTINCT FROM :firstName)
          OR (:lastName IS NOT NULL AND last_name IS DISTINCT FROM :lastName)
      )
    """, nativeQuery = true)
  int synchronizeIdentity(
      @Param("userId") String userId,
      @Param("email") String email,
      @Param("firstName") String firstName,
      @Param("lastName") String lastName
  );
}