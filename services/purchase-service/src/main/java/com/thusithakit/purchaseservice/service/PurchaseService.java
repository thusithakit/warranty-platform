package com.thusithakit.purchaseservice.service;

import com.thusithakit.purchaseservice.dto.CreatePurchaseRequestDTO;
import com.thusithakit.purchaseservice.dto.PurchaseResponseDTO;
import com.thusithakit.purchaseservice.dto.UpdatePurchaseRequestDTO;
import com.thusithakit.purchaseservice.entity.OutboxEvent;
import com.thusithakit.purchaseservice.entity.Purchase;
import com.thusithakit.purchaseservice.entity.PurchaseStatus;
import com.thusithakit.purchaseservice.exception.ResourceNotFoundException;
import com.thusithakit.purchaseservice.repository.OutboxEventRepository;
import com.thusithakit.purchaseservice.repository.PurchaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

@Service
public class PurchaseService {

  private final PurchaseRepository purchaseRepository;
  private final OutboxEventRepository outboxRepository;
  private final ObjectMapper objectMapper;

  public PurchaseService(
      PurchaseRepository purchaseRepository,
      OutboxEventRepository outboxRepository,
      ObjectMapper objectMapper
  ) {
    this.purchaseRepository = purchaseRepository;
    this.outboxRepository = outboxRepository;
    this.objectMapper = objectMapper;
  }

  @Transactional
  public PurchaseResponseDTO create(
      String userId,
      CreatePurchaseRequestDTO request
  ) {
    Purchase purchase = new Purchase();

    purchase.setUserId(userId);
    purchase.setItemName(request.itemName());
    purchase.setBrand(request.brand());
    purchase.setModel(request.model());
    purchase.setPurchasePrice(request.purchasePrice());
    purchase.setCurrency(
        request.currency() == null ? "LKR" : request.currency()
    );
    purchase.setSellerName(request.sellerName());
    purchase.setPurchaseDate(request.purchaseDate());
    purchase.setNotes(request.notes());
    purchase.setStatus(PurchaseStatus.ACTIVE);

    Purchase saved = purchaseRepository.save(purchase);

    createOutboxEvent(saved);

    return toResponse(saved);
  }

  @Transactional(readOnly = true)
  public List<PurchaseResponseDTO> getAll(String userId) {

    return purchaseRepository
        .findAllByUserIdAndStatusNotOrderByPurchaseDateDesc(
            userId,
            PurchaseStatus.DELETED
        )
        .stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public PurchaseResponseDTO getById(UUID id, String userId) {

    Purchase purchase = findOwnedPurchase(id, userId);

    return toResponse(purchase);
  }

  @Transactional
  public PurchaseResponseDTO update(
      UUID id,
      String userId,
      UpdatePurchaseRequestDTO request
  ) {
    Purchase purchase = findOwnedPurchase(id, userId);

    purchase.setItemName(request.itemName());
    purchase.setBrand(request.brand());
    purchase.setModel(request.model());
    purchase.setPurchasePrice(request.purchasePrice());
    purchase.setCurrency(request.currency());
    purchase.setSellerName(request.sellerName());
    purchase.setPurchaseDate(request.purchaseDate());
    purchase.setNotes(request.notes());

    return toResponse(purchaseRepository.save(purchase));
  }

  @Transactional
  public void delete(UUID id, String userId) {

    Purchase purchase = findOwnedPurchase(id, userId);

    // Soft deletion keeps references intact for warranty records.
    purchase.setStatus(PurchaseStatus.DELETED);

    purchaseRepository.save(purchase);
  }

  private Purchase findOwnedPurchase(UUID id, String userId) {

    return purchaseRepository.findByIdAndUserId(id, userId)
        .orElseThrow(() ->
            new ResourceNotFoundException("Purchase not found")
        );
  }

  private void createOutboxEvent(Purchase purchase) {

    try {
      JsonNode payload = objectMapper.valueToTree(
          new PurchaseCreatedEvent(
              purchase.getId(),
              purchase.getUserId(),
              purchase.getItemName(),
              purchase.getPurchaseDate()
          )
      );

      OutboxEvent event = new OutboxEvent();

      event.setAggregateType("PURCHASE");
      event.setAggregateId(purchase.getId());
      event.setEventType("PurchaseCreated");
      event.setEventVersion(1);
      event.setPayload(payload);
      event.setStatus("PENDING");

      outboxRepository.save(event);

    } catch (JacksonException exception) {
      throw new IllegalStateException(
          "Could not create purchase event",
          exception
      );
    }
  }

  private PurchaseResponseDTO toResponse(Purchase purchase) {

    return new PurchaseResponseDTO(
        purchase.getId(),
        purchase.getItemName(),
        purchase.getBrand(),
        purchase.getModel(),
        purchase.getPurchasePrice(),
        purchase.getCurrency(),
        purchase.getSellerName(),
        purchase.getPurchaseDate(),
        purchase.getNotes(),
        purchase.getStatus(),
        purchase.getCreatedAt(),
        purchase.getUpdatedAt()
    );
  }

  private record PurchaseCreatedEvent(
      UUID purchaseId,
      String userId,
      String itemName,
      java.time.LocalDate purchaseDate
  ) {}
}
