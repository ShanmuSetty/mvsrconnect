// ─── CanteenAdminController.java ────────────────────────────────────────────
package com.mvsr.mvsrconnect.controller;

import com.mvsr.mvsrconnect.service.CanteenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/canteen/payment")
public class CanteenPaymentController {

    @Autowired private CanteenService canteenService;

    /**
     * Razorpay sends POST here when payment is completed.
     * Must be in SecurityConfig permitAll — no auth needed.
     */
    @PostMapping("/webhook")
    public ResponseEntity<?> webhook(
            @RequestBody String rawBody,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {
        try {
            canteenService.handleWebhook(rawBody, signature);
            return ResponseEntity.ok(Map.of("status", "ok"));
        } catch (SecurityException e) {
            return ResponseEntity.status(400).body(Map.of("error", "Invalid signature"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
