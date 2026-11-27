package com.thusithakit.warrantyservice.repository;

import com.thusithakit.warrantyservice.entity.WarrantyOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface WarrantyOutboxEventRepository
    extends JpaRepository<WarrantyOutboxEvent, UUID> {

  @Query(value = """
        SELECT *
        FROM warranty_outbox_events
        WHERE status = 'PENDING'
          AND next_attempt_at <= :now
        ORDER BY created_at ASC
        LIMIT :batchSize
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
  List<WarrantyOutboxEvent> findPendingForUpdate(
      @Param("now") Instant now,
      @Param("batchSize") int batchSize
  );

  @Modifying
  @Query("""
        UPDATE WarrantyOutboxEvent e
        SET e.status = 'PENDING',
            e.lockedAt = NULL,
            e.lastError = 'Publisher claim lease expired'
        WHERE e.status = 'PROCESSING'
          AND e.lockedAt < :cutoff
        """)
  int recoverExpiredClaims(@Param("cutoff") Instant cutoff);
}
