package com.thusithakit.purchaseservice.repository;

import com.thusithakit.purchaseservice.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseRepository
    extends JpaRepository<Purchase, UUID> {

  List<Purchase> findAllByUserIdAndStatusNotOrderByPurchaseDateDesc(
      String userId,
      com.thusithakit.purchaseservice.entity.PurchaseStatus status
  );

  Optional<Purchase> findByIdAndUserId(UUID id, String userId);
}
