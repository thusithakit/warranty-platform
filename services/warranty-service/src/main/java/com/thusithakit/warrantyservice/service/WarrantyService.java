package com.thusithakit.warrantyservice.service;

import com.thusithakit.warrantyservice.dto.request.CreateWarrantyRequest;
import com.thusithakit.warrantyservice.dto.request.UpdateWarrantyRequest;
import com.thusithakit.warrantyservice.dto.response.WarrantyResponse;
import com.thusithakit.warrantyservice.entity.PurchaseReference;
import com.thusithakit.warrantyservice.entity.Warranty;
import com.thusithakit.warrantyservice.entity.WarrantyStatus;
import com.thusithakit.warrantyservice.repository.PurchaseReferenceRepository;
import com.thusithakit.warrantyservice.repository.WarrantyRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WarrantyService {

  private final WarrantyRepository warrantyRepository;
  private final PurchaseReferenceRepository purchaseReferenceRepository;

  @Transactional
  public WarrantyResponse create(
      String userId,
      CreateWarrantyRequest request
  ) {
    PurchaseReference purchase = purchaseReferenceRepository
        .findByPurchaseIdAndUserId(request.purchaseId(), userId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Purchase not found"
        ));

    Warranty warranty = new Warranty();

    warranty.setUserId(userId);
    warranty.setPurchaseId(purchase.getPurchaseId());
    warranty.setWarrantyProvider(request.warrantyProvider());
    warranty.setWarrantyReference(request.warrantyReference());
    warranty.setWarrantyType(request.warrantyType());
    warranty.setStartDate(request.startDate());
    warranty.setEndDate(request.endDate());
    warranty.setCoverageDescription(request.coverageDescription());
    warranty.setStatus(WarrantyStatus.ACTIVE);

    return toResponse(warrantyRepository.save(warranty));
  }

  @Transactional(readOnly = true)
  public List<WarrantyResponse> findAll(String userId) {
    return warrantyRepository.findAllByUserId(userId)
        .stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public WarrantyResponse findById(String userId, UUID warrantyId) {
    return toResponse(findOwnedWarranty(userId, warrantyId));
  }

  @Transactional
  public WarrantyResponse update(
      String userId,
      UUID warrantyId,
      UpdateWarrantyRequest request
  ) {
    Warranty warranty = findOwnedWarranty(userId, warrantyId);

    if (warranty.getStatus() == WarrantyStatus.VOIDED) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "A voided warranty cannot be updated"
      );
    }

    warranty.setWarrantyProvider(request.warrantyProvider());
    warranty.setWarrantyReference(request.warrantyReference());
    warranty.setWarrantyType(request.warrantyType());
    warranty.setStartDate(request.startDate());
    warranty.setEndDate(request.endDate());
    warranty.setCoverageDescription(request.coverageDescription());

    return toResponse(warrantyRepository.save(warranty));
  }

  @Transactional
  public void voidWarranty(String userId, UUID warrantyId) {
    Warranty warranty = findOwnedWarranty(userId, warrantyId);

    if (warranty.getStatus() == WarrantyStatus.VOIDED) {
      return;
    }

    warranty.setStatus(WarrantyStatus.VOIDED);
    warrantyRepository.save(warranty);
  }

  private Warranty findOwnedWarranty(String userId, UUID warrantyId) {
    return warrantyRepository.findByIdAndUserId(warrantyId, userId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Warranty not found"
        ));
  }

  private WarrantyResponse toResponse(Warranty warranty) {
    return new WarrantyResponse(
        warranty.getId(),
        warranty.getPurchaseId(),
        warranty.getWarrantyProvider(),
        warranty.getWarrantyReference(),
        warranty.getWarrantyType(),
        warranty.getStartDate(),
        warranty.getEndDate(),
        warranty.getCoverageDescription(),
        warranty.getStatus(),
        warranty.getCreatedAt(),
        warranty.getUpdatedAt()
    );
  }
}
