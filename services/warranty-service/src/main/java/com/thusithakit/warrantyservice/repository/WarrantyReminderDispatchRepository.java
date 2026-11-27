package com.thusithakit.warrantyservice.repository;

import com.thusithakit.warrantyservice.entity.WarrantyReminderDispatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface WarrantyReminderDispatchRepository
    extends JpaRepository<WarrantyReminderDispatch, UUID> {

  @Modifying
  @Query(value = """
        INSERT INTO warranty_reminder_dispatches (
            warranty_id,
            user_id,
            purchase_id,
            reminder_days_before,
            scheduled_for,
            status
        )
        VALUES (
            :warrantyId,
            :userId,
            :purchaseId,
            :daysBefore,
            :scheduledFor,
            'PENDING'
        )
        ON CONFLICT (warranty_id, reminder_days_before)
        DO NOTHING
        """, nativeQuery = true)
  int insertIfAbsent(
      @Param("warrantyId") UUID warrantyId,
      @Param("userId") String userId,
      @Param("purchaseId") UUID purchaseId,
      @Param("daysBefore") Integer daysBefore,
      @Param("scheduledFor") LocalDate scheduledFor
  );

  @Modifying
  @Query("""
        UPDATE WarrantyReminderDispatch r
        SET r.status = :status,
            r.dispatchedAt = :dispatchedAt
        WHERE r.id = :id
          AND r.status = :expectedStatus
    """)
  int updateStatus(
      @Param("id") UUID id,
      @Param("status") WarrantyReminderDispatch.Status status,
      @Param("expectedStatus") WarrantyReminderDispatch.Status expectedStatus,
      @Param("dispatchedAt") Instant dispatchedAt
  );

  @Query("""
    SELECT r
    FROM WarrantyReminderDispatch r
    WHERE r.warrantyId = :warrantyId
      AND r.reminderDaysBefore = :daysBefore
    """)
  Optional<WarrantyReminderDispatch> findByWarrantyAndDaysBefore(
      @Param("warrantyId") UUID warrantyId,
      @Param("daysBefore") Integer daysBefore
  );
}