package com.mvsr.mvsrconnect.controller;

import com.mvsr.mvsrconnect.model.PushSubscription;
import com.mvsr.mvsrconnect.model.User;
import com.mvsr.mvsrconnect.repository.PushSubscriptionRepository;
import com.mvsr.mvsrconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/push")
public class PushController {

    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final UserRepository userRepository;

    @Value("${vapid.public.key}")
    private String vapidPublicKey;

    public PushController(
            PushSubscriptionRepository pushSubscriptionRepository,
            UserRepository userRepository) {
        this.pushSubscriptionRepository = pushSubscriptionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Returns the VAPID public key so the frontend can subscribe.
     * This endpoint must be publicly accessible (no auth needed).
     */
    @GetMapping("/vapid-key")
    public ResponseEntity<Map<String, String>> getVapidKey() {
        return ResponseEntity.ok(Map.of("publicKey", vapidPublicKey));
    }

    /**
     * Save a new push subscription for the logged-in user.
     * Called by the frontend after the user grants notification permission.
     *
     * Expected body:
     * {
     *   "endpoint": "https://fcm.googleapis.com/...",
     *   "keys": {
     *     "p256dh": "...",
     *     "auth": "..."
     *   }
     * }
     */
    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal OAuth2User oauthUser) {

        String email = oauthUser.getAttribute("email");
        User user = userRepository.findByEmail(email).orElseThrow();

        String endpoint = (String) body.get("endpoint");

        @SuppressWarnings("unchecked")
        Map<String, String> keys = (Map<String, String>) body.get("keys");
        String p256dh = keys.get("p256dh");
        String auth = keys.get("auth");

        // Upsert — if endpoint already exists for this user, update keys
        PushSubscription sub = pushSubscriptionRepository.findByEndpoint(endpoint)
                .orElse(new PushSubscription());

        sub.setUser(user);
        sub.setEndpoint(endpoint);
        sub.setP256dh(p256dh);
        sub.setAuth(auth);
        if (sub.getCreatedAt() == null) sub.setCreatedAt(LocalDateTime.now());

        pushSubscriptionRepository.save(sub);

        return ResponseEntity.ok(Map.of("status", "subscribed"));
    }

    /**
     * Remove a push subscription (user turned off notifications).
     */
    @PostMapping("/unsubscribe")
    public ResponseEntity<?> unsubscribe(@RequestBody Map<String, String> body) {
        String endpoint = body.get("endpoint");
        if (endpoint != null) {
            pushSubscriptionRepository.deleteByEndpoint(endpoint);
        }
        return ResponseEntity.ok(Map.of("status", "unsubscribed"));
    }
}
