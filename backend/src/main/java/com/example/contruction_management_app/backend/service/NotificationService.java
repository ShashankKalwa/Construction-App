package com.example.contruction_management_app.backend.service;

import com.example.contruction_management_app.backend.domain.*;
import com.example.contruction_management_app.backend.dto.*;
import com.example.contruction_management_app.backend.exception.ResourceNotFoundException;
import com.example.contruction_management_app.backend.repository.*;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final ProjectRepository projectRepository;

    public NotificationService(
        NotificationRepository notificationRepository,
        UserRepository userRepository,
        OrganizationRepository organizationRepository,
        ProjectRepository projectRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getUserNotifications(UserPrincipal principal) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(principal.getId()).stream()
            .map(NotificationDto::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UserPrincipal principal) {
        return notificationRepository.countByUserIdAndIsReadFalse(principal.getId());
    }

    @Transactional
    public NotificationDto markAsRead(String notificationId, UserPrincipal principal) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));
        notification.setIsRead(true);
        Notification saved = notificationRepository.save(notification);
        return NotificationDto.fromEntity(saved);
    }

    @Transactional
    public void markAllAsRead(UserPrincipal principal) {
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(principal.getId());
        for (Notification n : unread) {
            n.setIsRead(true);
        }
        notificationRepository.saveAll(unread);
    }

    @Transactional
    public void sendNotification(
        String organizationId,
        String userId,
        String projectId,
        String title,
        String message,
        NotificationType type,
        String entityId,
        String actionUrl
    ) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        Organization org = organizationRepository.findById(organizationId).orElse(null);
        Project project = (projectId != null) ? projectRepository.findById(projectId).orElse(null) : null;

        Notification notification = Notification.builder()
            .id(UUID.randomUUID().toString())
            .organization(org)
            .user(user)
            .project(project)
            .title(title)
            .message(message)
            .type(type)
            .isRead(false)
            .entityId(entityId)
            .actionUrl(actionUrl)
            .createdAt(Instant.now())
            .build();

        notificationRepository.save(notification);
    }
}
