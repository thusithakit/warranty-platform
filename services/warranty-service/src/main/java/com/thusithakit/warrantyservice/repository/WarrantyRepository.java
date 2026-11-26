package com.thusithakit.warrantyservice.repository;

import com.thusithakit.warrantyservice.entity.Warranty;
import org.springframework.data.jpa.repository.JpaRepository;

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
}