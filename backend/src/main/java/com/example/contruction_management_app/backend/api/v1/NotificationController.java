package com.example.contruction_management_app.backend.api.v1;

import com.example.contruction_management_app.backend.dto.NotificationDto;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import com.example.contruction_management_app.backend.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationDto>> getNotifications(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(notificationService.getUserNotifications(principal));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(Map.of("unreadCount", notificationService.getUnreadCount(principal)));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationDto> markAsRead(
        @PathVariable String notificationId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(notificationService.markAsRead(notificationId, principal));
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<Map<String, String>> markAllAsRead(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        notificationService.markAllAsRead(principal);
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
    }
}
