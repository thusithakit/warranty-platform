package com.thusithakit.purchaseservice.repository;

import com.thusithakit.purchaseservice.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository
    extends JpaRepository<OutboxEvent, UUID> {

  @Query(value = """
        SELECT *
        FROM outbox_events
        WHERE status = 'PENDING'
          AND next_attempt_at <= :now
        ORDER BY created_at ASC
        LIMIT :batchSize
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
  List<OutboxEvent> findPendingEventsForUpdate(
      @Param("now") Instant now,
      @Param("batchSize") int batchSize
  );

  @Modifying
  @Query("""
        UPDATE OutboxEvent e
        SET e.status = 'PENDING',
            e.lockedAt = null
        WHERE e.status = 'PROCESSING'
          AND e.lockedAt < :leaseExpiry
        """)
  int releaseExpiredClaims(
      @Param("leaseExpiry") Instant leaseExpiry
  );
}
