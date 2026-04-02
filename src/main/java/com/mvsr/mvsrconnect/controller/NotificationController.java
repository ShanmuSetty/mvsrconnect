package com.mvsr.mvsrconnect.controller;

import com.mvsr.mvsrconnect.model.Notification;
import com.mvsr.mvsrconnect.model.User;
import com.mvsr.mvsrconnect.repository.NotificationRepository;
import com.mvsr.mvsrconnect.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationController(
            NotificationRepository notificationRepository,
            UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    /**
     * Last 20 notifications for the bell dropdown.
     */
    @GetMapping
    public List<Notification> getNotifications(
            @AuthenticationPrincipal OAuth2User oauthUser) {
        String email = oauthUser.getAttribute("email");
        User user = userRepository.findByEmail(email).orElseThrow();
        return notificationRepository.findTop20ByUserIdOrderByCreatedAtDesc(user.getId());
    }

    /**
     * Unread count — polled by the frontend to update the bell badge.
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(
            @AuthenticationPrincipal OAuth2User oauthUser) {
        String email = oauthUser.getAttribute("email");
        User user = userRepository.findByEmail(email).orElseThrow();
        long count = notificationRepository.countByUserIdAndReadFalse(user.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * Mark all notifications as read (called when user opens the bell dropdown).
     */
    @PostMapping("/read-all")
    public ResponseEntity<?> markAllRead(
            @AuthenticationPrincipal OAuth2User oauthUser) {
        String email = oauthUser.getAttribute("email");
        User user = userRepository.findByEmail(email).orElseThrow();
        notificationRepository.markAllReadByUserId(user.getId());
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
