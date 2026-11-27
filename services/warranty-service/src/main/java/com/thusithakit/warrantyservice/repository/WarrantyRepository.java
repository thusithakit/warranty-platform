package com.thusithakit.warrantyservice.repository;

import com.thusithakit.warrantyservice.entity.Warranty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WarrantyRepository
    extends JpaRepository<Warranty, UUID> {

  List<Warranty> findAllByUserId(String userId);

  List<Warranty> findAllByPurchaseIdAndUserId(
      UUID purchaseId,
      String userId
  );

  Optional<Warranty> findByIdAndUserId(
      UUID id,
      String userId
  );

  @Query(value = """
    SELECT *
    FROM warranties
    WHERE status = 'ACTIVE'
      AND end_date IS NOT NULL
      AND end_date < :today
    ORDER BY end_date ASC
    LIMIT :batchSize
    FOR UPDATE SKIP LOCKED
    """, nativeQuery = true)
  List<Warranty> findExpiredWarrantiesForUpdate(
      @Param("today") LocalDate today,
      @Param("batchSize") int batchSize
  );
}