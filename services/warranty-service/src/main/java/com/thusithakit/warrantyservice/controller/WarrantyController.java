package com.thusithakit.warrantyservice.controller;

import com.thusithakit.warrantyservice.dto.request.CreateWarrantyRequest;
import com.thusithakit.warrantyservice.dto.request.UpdateWarrantyRequest;
import com.thusithakit.warrantyservice.dto.response.WarrantyResponse;
import com.thusithakit.warrantyservice.service.WarrantyReminderService;
import com.thusithakit.warrantyservice.service.WarrantyService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/warranties")
@RequiredArgsConstructor
public class WarrantyController {

  private final WarrantyService warrantyService;
  private final WarrantyReminderService reminderService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public WarrantyResponse create(
      @AuthenticationPrincipal Jwt jwt,
      @Valid @RequestBody CreateWarrantyRequest request
  ) {
    return warrantyService.create(jwt.getSubject(), request);
  }

  @GetMapping
  public List<WarrantyResponse> findAll(
      @AuthenticationPrincipal Jwt jwt
  ) {
    return warrantyService.findAll(jwt.getSubject());
  }

  @GetMapping("/{id}")
  public WarrantyResponse findById(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID id
  ) {
    return warrantyService.findById(jwt.getSubject(), id);
  }

  @PutMapping("/{id}")
  public WarrantyResponse update(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID id,
      @Valid @RequestBody UpdateWarrantyRequest request
  ) {
    return warrantyService.update(jwt.getSubject(), id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID id
  ) {
    warrantyService.voidWarranty(jwt.getSubject(), id);
  }

  @PostMapping("/internal/reminders/generate")
  public int generateReminders() {
    return reminderService.generateDueReminders();
  }
}
