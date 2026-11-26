package com.thusithakit.purchaseservice.controller;

import com.thusithakit.purchaseservice.dto.CreatePurchaseRequestDTO;
import com.thusithakit.purchaseservice.dto.PurchaseResponseDTO;
import com.thusithakit.purchaseservice.dto.UpdatePurchaseRequestDTO;
import com.thusithakit.purchaseservice.service.PurchaseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/purchases")
public class PurchaseController {

  private final PurchaseService purchaseService;

  public PurchaseController(PurchaseService purchaseService) {
    this.purchaseService = purchaseService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public PurchaseResponseDTO create(
      @AuthenticationPrincipal Jwt jwt,
      @Valid @RequestBody CreatePurchaseRequestDTO request
  ) {
    return purchaseService.create(jwt.getSubject(), request);
  }

  @GetMapping
  public List<PurchaseResponseDTO> getAll(
      @AuthenticationPrincipal Jwt jwt
  ) {
    return purchaseService.getAll(jwt.getSubject());
  }

  @GetMapping("/{id}")
  public PurchaseResponseDTO getById(
      @PathVariable UUID id,
      @AuthenticationPrincipal Jwt jwt
  ) {
    return purchaseService.getById(id, jwt.getSubject());
  }

  @PutMapping("/{id}")
  public PurchaseResponseDTO update(
      @PathVariable UUID id,
      @AuthenticationPrincipal Jwt jwt,
      @Valid @RequestBody UpdatePurchaseRequestDTO request
  ) {
    return purchaseService.update(
        id,
        jwt.getSubject(),
        request
    );
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @PathVariable UUID id,
      @AuthenticationPrincipal Jwt jwt
  ) {
    purchaseService.delete(id, jwt.getSubject());
  }
}
