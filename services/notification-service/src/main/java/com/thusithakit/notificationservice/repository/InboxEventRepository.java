package com.thusithakit.notificationservice.repository;

import com.thusithakit.notificationservice.entity.InboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface InboxEventRepository
    extends JpaRepository<InboxEvent, UUID> {

  @Modifying
  @Query(value = """
        INSERT INTO inbox_events (
            event_id,
            event_type,
            aggregate_id,
            consumer_name
        )
        VALUES (
            :eventId,
            :eventType,
            :aggregateId,
            :consumerName
        )
        ON CONFLICT (event_id) DO NOTHING
        """, nativeQuery = true)
  int insertIfNotProcessed(
      @Param("eventId") UUID eventId,
      @Param("eventType") String eventType,
      @Param("aggregateId") UUID aggregateId,
      @Param("consumerName") String consumerName
  );
}
