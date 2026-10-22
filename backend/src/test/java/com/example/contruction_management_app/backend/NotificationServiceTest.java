package com.example.contruction_management_app.backend;

import com.example.contruction_management_app.backend.domain.*;
import com.example.contruction_management_app.backend.dto.NotificationDto;
import com.example.contruction_management_app.backend.repository.*;
import com.example.contruction_management_app.backend.security.UserPrincipal;
import com.example.contruction_management_app.backend.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class NotificationServiceTest {

    private NotificationRepository notificationRepository;
    private UserRepository userRepository;
    private OrganizationRepository organizationRepository;
    private ProjectRepository projectRepository;
    private NotificationService notificationService;

    private UserPrincipal testPrincipal;
    private User mockUser;
    private Organization mockOrg;

    @BeforeEach
    void setUp() {
        notificationRepository = Mockito.mock(NotificationRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        organizationRepository = Mockito.mock(OrganizationRepository.class);
        projectRepository = Mockito.mock(ProjectRepository.class);

        notificationService = new NotificationService(
            notificationRepository,
            userRepository,
            organizationRepository,
            projectRepository
        );

        testPrincipal = new UserPrincipal(
            "usr_pm_rahul", "rahul@apex.com", "hash",
            "org_apex_infra", "pm", List.of(), true
        );

        mockOrg = new Organization("Apex Infrastructure", "apex-infra");
        mockOrg.setId("org_apex_infra");

        mockUser = new User();
        mockUser.setId("usr_pm_rahul");
        mockUser.setEmail("rahul@apex.com");
    }

    @Test
    void testGetUnreadCountAndNotifications() {
        Notification n1 = Notification.builder()
            .id("notif_1")
            .user(mockUser)
            .organization(mockOrg)
            .title("Low Stock: Cement")
            .message("Current stock below threshold")
            .type(NotificationType.LOW_STOCK)
            .isRead(false)
            .createdAt(Instant.now())
            .build();

        when(notificationRepository.findByUserIdOrderByCreatedAtDesc("usr_pm_rahul")).thenReturn(List.of(n1));
        when(notificationRepository.countByUserIdAndIsReadFalse("usr_pm_rahul")).thenReturn(1L);

        long unreadCount = notificationService.getUnreadCount(testPrincipal);
        assertEquals(1L, unreadCount);

        List<NotificationDto> notifications = notificationService.getUserNotifications(testPrincipal);
        assertEquals(1, notifications.size());
        assertEquals("Low Stock: Cement", notifications.get(0).title());
        assertFalse(notifications.get(0).isRead());
    }

    @Test
    void testMarkAsRead() {
        Notification n = Notification.builder()
            .id("notif_1")
            .user(mockUser)
            .organization(mockOrg)
            .title("Bill Submitted")
            .message("RA Bill #4 ready for approval")
            .type(NotificationType.BILL_SUBMITTED)
            .isRead(false)
            .createdAt(Instant.now())
            .build();

        when(notificationRepository.findById("notif_1")).thenReturn(Optional.of(n));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificationDto result = notificationService.markAsRead("notif_1", testPrincipal);

        assertNotNull(result);
        assertTrue(result.isRead());
    }
}
