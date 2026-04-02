package com.mvsr.mvsrconnect.service;

import com.mvsr.mvsrconnect.model.PushSubscription;
import com.mvsr.mvsrconnect.model.User;
import com.mvsr.mvsrconnect.repository.NotificationRepository;
import com.mvsr.mvsrconnect.repository.PushSubscriptionRepository;
import com.mvsr.mvsrconnect.repository.UserRepository;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.apache.http.HttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.security.Security;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PushNotificationService {

    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final PushService pushService;

    @Value("${vapid.public.key}")
    private String vapidPublicKey;

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public PushNotificationService(
            PushSubscriptionRepository pushSubscriptionRepository,
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            @Value("${vapid.public.key}") String publicKey,
            @Value("${vapid.private.key}") String privateKey,
            @Value("${vapid.subject}") String subject) throws Exception {

        this.pushSubscriptionRepository = pushSubscriptionRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.pushService = new PushService(publicKey, privateKey, subject);
    }

    /**
     * Send a push notification to a specific user by userId.
     * Also saves a Notification log entry for the bell dropdown.
     */
    @Async
    public void sendToUser(Long userId, String title, String body, String url) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        // 1. Log to DB (for bell dropdown)
        saveNotification(user, title, body, url);

        // 2. Send Web Push to all registered browser subscriptions
        List<PushSubscription> subs = pushSubscriptionRepository.findByUserId(userId);
        if (subs.isEmpty()) return;

        String payload = buildPayload(title, body, url);

        for (PushSubscription sub : subs) {
            try {
                Subscription subscription = new Subscription(
                        sub.getEndpoint(),
                        new Subscription.Keys(sub.getP256dh(), sub.getAuth())
                );

                nl.martijndwars.webpush.Notification notification =
                        new nl.martijndwars.webpush.Notification(subscription, payload);

                HttpResponse response = pushService.send(notification);
                System.out.println("Push Status: " + response.getStatusLine());

            } catch (Exception e) {
                System.out.println("Push failed for endpoint " + sub.getEndpoint() + ": " + e.getMessage());
                // If the subscription is gone (410 Gone), clean it up
                if (e.getMessage() != null && e.getMessage().contains("410")) {
                    pushSubscriptionRepository.deleteByEndpoint(sub.getEndpoint());
                }
            }
        }
    }

    /**
     * Convenience: send by user email instead of ID
     */
    @Async
    public void sendToEmail(String email, String title, String body, String url) {
        userRepository.findByEmail(email).ifPresent(user ->
                sendToUser(user.getId(), title, body, url));
    }

    // ── Specific notification helpers ──

    public void notifyCommentOnPost(Long postAuthorId, String commenterName, Long postId) {
        sendToUser(postAuthorId,
                "New comment on your post",
                commenterName + " commented on your post",
                "/?post=" + postId);
    }

    public void notifyReplyToComment(Long commentAuthorId, String replierName, Long postId) {
        sendToUser(commentAuthorId,
                "Someone replied to your comment",
                replierName + " replied to your comment",
                "/?post=" + postId);
    }

    public void notifyClubJoinApproved(Long userId, String clubName, Long clubId) {
        sendToUser(userId,
                "Join request approved 🎉",
                "You're now a member of " + clubName,
                "/");
    }

    public void notifyClubJoinRejected(Long userId, String clubName) {
        sendToUser(userId,
                "Join request not approved",
                "Your request to join " + clubName + " was rejected",
                "/");
    }

    public void notifyModeratorApproved(Long userId, String clubName) {
        sendToUser(userId,
                "You're now a Moderator 🎉",
                "Your appeal for " + clubName + " was approved",
                "/mod.html");
    }

    public void notifyModeratorRejected(Long userId, String clubName) {
        sendToUser(userId,
                "Moderator appeal update",
                "Your appeal for " + clubName + " was not approved",
                "/");
    }

    public void notifyLostFoundResponse(Long itemOwnerId, String responderName, Long itemId) {
        sendToUser(itemOwnerId,
                "Someone responded to your item",
                responderName + " responded to your Lost & Found post",
                "/lostandfound.html");
    }

    public void notifyEventEnrollmentApproved(Long userId, String eventTitle) {
        sendToUser(userId,
                "Enrollment confirmed 🎟️",
                "Your spot for \"" + eventTitle + "\" is confirmed. Check your QR ticket!",
                "/events.html");
    }

    public void notifyNewClubPost(Long memberId, String authorName, String clubName, Long postId) {
        sendToUser(memberId,
                "New post in " + clubName,
                authorName + " posted in " + clubName,
                "/?post=" + postId);
    }

    // ── Internals ──

    private void saveNotification(User user, String title, String body, String url) {
        com.mvsr.mvsrconnect.model.Notification n = new com.mvsr.mvsrconnect.model.Notification();
        n.setUser(user);
        n.setTitle(title);
        n.setBody(body);
        n.setUrl(url);
        n.setRead(false);
        n.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(n);
    }

    private String buildPayload(String title, String body, String url) {
        return new JSONObject()
                .put("title", title)
                .put("body", body)
                .put("url", url)
                .put("icon", "/icon-192.png")
                .put("badge", "/badge-72.png")
                .toString();
    }
}
