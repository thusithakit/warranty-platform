package com.thusithakit.notificationservice.controller;

import com.thusithakit.notificationservice.dto.NotificationResponse;
import com.thusithakit.notificationservice.dto.UnreadCountResponse;
import com.thusithakit.notificationservice.service.NotificationManagementService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

  private final NotificationManagementService notificationService;

  public NotificationController(
      NotificationManagementService notificationService
  ) {
    this.notificationService = notificationService;
  }

  @GetMapping
  public Page<NotificationResponse> getNotifications(
      @AuthenticationPrincipal Jwt jwt,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    return notificationService.findAll(
        jwt.getSubject(),
        page,
        size
    );
  }

  @GetMapping("/unread")
  public Page<NotificationResponse> getUnreadNotifications(
      @AuthenticationPrincipal Jwt jwt,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    return notificationService.findUnread(
        jwt.getSubject(),
        page,
        size
    );
  }

  @GetMapping("/unread-count")
  public UnreadCountResponse getUnreadCount(
      @AuthenticationPrincipal Jwt jwt
  ) {
    return new UnreadCountResponse(
        notificationService.getUnreadCount(jwt.getSubject())
    );
  }

  @PatchMapping("/{id}/read")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void markAsRead(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID id
  ) {
    notificationService.markAsRead(jwt.getSubject(), id);
  }

  @PatchMapping("/read-all")
  public Map<String, Integer> markAllAsRead(
      @AuthenticationPrincipal Jwt jwt
  ) {
    int updated = notificationService.markAllAsRead(
        jwt.getSubject()
    );

    return Map.of("updatedCount", updated);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void archiveNotification(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable UUID id
  ) {
    notificationService.archive(jwt.getSubject(), id);
  }
}
