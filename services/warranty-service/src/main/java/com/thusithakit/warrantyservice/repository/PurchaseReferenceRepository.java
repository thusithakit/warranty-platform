package com.thusithakit.warrantyservice.repository;

import com.thusithakit.warrantyservice.entity.PurchaseReference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseReferenceRepository
    extends JpaRepository<PurchaseReference, UUID> {

  Optional<PurchaseReference> findByPurchaseIdAndUserId(
      UUID purchaseId,
      String userId
  );

  List<PurchaseReference> findAllByUserId(String userId);
}