package com.mvsr.mvsrconnect.service;

import com.mvsr.mvsrconnect.model.*;
import com.mvsr.mvsrconnect.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CanteenService {

    @Autowired private CanteenOrderRepository orderRepo;
    @Autowired private OrderItemRepository itemRepo;
    @Autowired private FoodStallRepository stallRepo;
    @Autowired private VendorSessionRepository sessionRepo;
    @Autowired private FoodItemRepository foodItemRepo;
    @Autowired private RazorpayService razorpayService;
    @Autowired private VendorSseService sseService;
    @Autowired private PushNotificationService pushNotificationService;

    @Value("${canteen.payment.mode:MANUAL}")
    private String paymentMode;

    @Value("${canteen.qr.secret}")
    private String qrSecret;

    @Value("${canteen.vendor.session.hours:12}")
    private int vendorSessionHours;

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    // ── Cart item DTO (receive from frontend) ──────────────────────────────
    public record CartItem(Long foodItemId, int quantity) {}

    // ── Place order ────────────────────────────────────────────────────────
    @Transactional
    public Map<String, Object> placeOrder(Long studentUserId, Long stallId,
                                          List<CartItem> cartItems,
                                          String studentName, String studentEmail) throws Exception {
        FoodStall stall = stallRepo.findById(stallId)
                .orElseThrow(() -> new IllegalArgumentException("Stall not found"));

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem ci : cartItems) {
            FoodItem fi = foodItemRepo.findById(ci.foodItemId())
                    .orElseThrow(() -> new IllegalArgumentException("Item not found: " + ci.foodItemId()));

            if (!fi.isAvailable()) throw new IllegalStateException(fi.getName() + " is not available");

            if (fi.getQuantityType() == QuantityType.COUNTABLE) {
                if (fi.getStockCount() < ci.quantity())
                    throw new IllegalStateException("Not enough stock for " + fi.getName());
                fi.setStockCount(fi.getStockCount() - ci.quantity());
                foodItemRepo.save(fi);
            }

            OrderItem oi = new OrderItem();
            oi.setFoodItemId(fi.getId());
            oi.setItemName(fi.getName());
            oi.setQuantity(ci.quantity());
            oi.setUnitPrice(fi.getPrice());
            orderItems.add(oi);
            total = total.add(fi.getPrice().multiply(BigDecimal.valueOf(ci.quantity())));
        }

        CanteenOrder order = new CanteenOrder();
        order.setStudentUserId(studentUserId);
        order.setStallId(stallId);
        order.setTotalAmount(total);
        order.setPaymentMode(paymentMode);
        order.setTokenNumber(orderRepo.nextTokenNumber(stallId));
        order = orderRepo.save(order);

        // Link items to order
        for (OrderItem oi : orderItems) {
            oi.setOrder(order);
        }
        itemRepo.saveAll(orderItems);
        order.setItems(orderItems);

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getId());
        result.put("tokenNumber", order.getTokenNumber());
        result.put("totalAmount", total);
        result.put("paymentMode", paymentMode);

        if ("AUTO".equalsIgnoreCase(paymentMode)) {
            Map<String, String> link = razorpayService.createPaymentLink(
                    order.getId(), total.doubleValue(), studentName, studentEmail);
            order.setRazorpayPaymentLinkId(link.get("id"));
            orderRepo.save(order);
            result.put("paymentUrl", link.get("url"));
        } else {
            // Manual — return stall UPI info
            result.put("upiId", stall.getUpiId());
            result.put("upiQrUrl", stall.getUpiQrUrl());
        }

        return result;
    }

    // ── Handle Razorpay webhook ────────────────────────────────────────────
    @Transactional
    public void handleWebhook(String rawBody, String signature) throws Exception {
        System.out.println("🔥 WEBHOOK HIT");
        System.out.println("Raw body: " + rawBody);
        if (!razorpayService.verifyWebhookSignature(rawBody, signature))
            throw new SecurityException("Invalid webhook signature");

        String linkId = razorpayService.extractPaymentLinkId(rawBody);
        String paymentId = razorpayService.extractPaymentId(rawBody);

        if (linkId == null) {
            System.out.println("❌ linkId is null");
            return;
        }

        orderRepo.findByRazorpayPaymentLinkId(linkId).ifPresent(order -> {
            order.setStatus("PAID");
            order.setRazorpayPaymentId(paymentId);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepo.save(order);
            pushNotificationService.sendToUser(
                    order.getStudentUserId(),
                    "Payment successful 💸",
                    "Your order #" + order.getTokenNumber() + " is confirmed!",
                    "/canteen.html?order=" + order.getId()
            );
            // Push to vendor dashboard via SSE
            sseService.pushOrderToVendor(order.getStallId(), buildOrderSummary(order));
            // Notify student
            sseService.pushStatusToStudent(order.getId(), "PAID");
        });
    }

    public String generateVerificationCode(CanteenOrder order) {
        try {
            String data = order.getId() + ":" + order.getStudentUserId() + ":" + order.getTotalAmount();
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(qrSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            String hex = HexFormat.of().formatHex(hash).substring(0, 16);
            return order.getId() + "." + hex;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate verification code", e);
        }
    }

    @Transactional
    public Map<String, Object> verifyAndMarkPaid(Long orderId, String paymentLinkId,
                                                 String paymentLinkReferenceId,
                                                 String paymentLinkStatus,
                                                 String paymentId,
                                                 String signature) {
        boolean valid = razorpayService.verifyPaymentLinkSignature(
                paymentLinkId, paymentLinkReferenceId, paymentLinkStatus, paymentId, signature);

        if (!valid) throw new SecurityException("Invalid payment signature");

        CanteenOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if ("PAID".equals(order.getStatus())) {
            return buildOrderSummary(order); // already paid, idempotent
        }

        order.setStatus("PAID");
        order.setRazorpayPaymentId(paymentId);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepo.save(order);
        pushNotificationService.sendToUser(order.getStudentUserId(),
                "Payment confirmed! 🎉",
                "Your canteen order #" + order.getTokenNumber() + " is confirmed. Preparing soon!",
                "/canteen.html?order=" + order.getId());

        sseService.pushOrderToVendor(order.getStallId(), buildOrderSummary(order));
        sseService.pushStatusToStudent(order.getId(), "PAID");

        return buildOrderSummary(order);
    }

    // ── Manual UTR submission ──────────────────────────────────────────────
    @Transactional
    public void submitUtr(Long orderId, Long studentUserId, String utr) {
        CanteenOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (!order.getStudentUserId().equals(studentUserId))
            throw new SecurityException("Not your order");
        order.setUtrNumber(utr);
        order.setStatus("PENDING_VENDOR_VERIFY");
        order.setUpdatedAt(LocalDateTime.now());
        orderRepo.save(order);
    }

    // ── Vendor: update order status ────────────────────────────────────────
    @Transactional
    public void updateOrderStatus(Long orderId, String newStatus, Long vendorStallId) {
        CanteenOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (!order.getStallId().equals(vendorStallId))
            throw new SecurityException("Order does not belong to your stall");
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepo.save(order);
        switch (newStatus) {
            case "PREPARING" -> pushNotificationService.sendToUser(order.getStudentUserId(),
                    "Order being prepared 👨‍🍳",
                    "Token #" + order.getTokenNumber() + " is being prepared!",
                    "/canteen.html?order=" + order.getId());

            case "READY" -> pushNotificationService.sendToUser(order.getStudentUserId(),
                    "Order ready! 🍽️",
                    "Token #" + order.getTokenNumber() + " is ready. Show it at the counter!",
                    "/canteen.html?order=" + order.getId());

            case "CANCELLED" -> pushNotificationService.sendToUser(order.getStudentUserId(),
                    "Order cancelled ❌",
                    "Your canteen order #" + order.getTokenNumber() + " was cancelled.",
                    "/canteen.html?order=" + order.getId());

            case "PICKED_UP" -> pushNotificationService.sendToUser(order.getStudentUserId(),
                    "Enjoy! 🎉",
                    "Order #" + order.getTokenNumber() + " picked up. Bon appétit!",
                    "/canteen.html?order=" + order.getId());

        }
        sseService.pushStatusToStudent(order.getId(), newStatus);
    }

    // ── Vendor: toggle item availability ──────────────────────────────────
    @Transactional
    public void toggleItem(Long itemId, boolean available, Long vendorStallId) {
        FoodItem fi = foodItemRepo.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        if (!fi.getStallId().equals(vendorStallId))
            throw new SecurityException("Item does not belong to your stall");
        fi.setAvailable(available);
        foodItemRepo.save(fi);
    }

    // ── Vendor: set stock count (COUNTABLE items) ─────────────────────────
    @Transactional
    public void setStock(Long itemId, int count, Long vendorStallId) {
        FoodItem fi = foodItemRepo.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        if (!fi.getStallId().equals(vendorStallId))
            throw new SecurityException("Item does not belong to your stall");
        fi.setStockCount(count);
        fi.setAvailable(count > 0);
        foodItemRepo.save(fi);
    }

    // ── Admin: generate vendor token ───────────────────────────────────────
    public String generateVendorToken(Long stallId) {
        FoodStall stall = stallRepo.findById(stallId)
                .orElseThrow(() -> new IllegalArgumentException("Stall not found"));
        String plainToken = UUID.randomUUID().toString();
        stall.setSetupTokenHash(bcrypt.encode(plainToken));
        stall.setTokenUsed(false);
        stallRepo.save(stall);
        return plainToken; // shown once to admin
    }

    // ── Vendor login: validate token, create session cookie value ─────────
    @Transactional
    public String vendorLogin(String plainToken, int sessionHours) {
        List<FoodStall> stalls = stallRepo.findAll();
        FoodStall matched = stalls.stream()
                .filter(s -> bcrypt.matches(plainToken, s.getSetupTokenHash()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        VendorSession session = new VendorSession();
        session.setStallId(matched.getId());
        session.setSessionToken(UUID.randomUUID().toString());
        session.setExpiresAt(LocalDateTime.now().plusHours(sessionHours));
        sessionRepo.save(session);

        return session.getSessionToken();
    }

    // ── Helper ────────────────────────────────────────────────────────────
    public Map<String, Object> buildOrderSummary(CanteenOrder order) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", order.getId());
        m.put("tokenNumber", order.getTokenNumber());
        m.put("totalAmount", order.getTotalAmount());
        m.put("status", order.getStatus());
        m.put("createdAt", order.getCreatedAt().toString());
        List<Map<String, Object>> items = new ArrayList<>();
        if (order.getItems() != null) {
            for (OrderItem oi : order.getItems()) {
                items.add(Map.of(
                        "name", oi.getItemName(),
                        "quantity", oi.getQuantity(),
                        "unitPrice", oi.getUnitPrice()
                ));
            }
        }
        m.put("items", items);
        m.put("verificationCode", generateVerificationCode(order));
        return m;
    }
}
