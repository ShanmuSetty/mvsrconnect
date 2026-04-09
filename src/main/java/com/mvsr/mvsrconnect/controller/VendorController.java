package com.mvsr.mvsrconnect.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.mvsr.mvsrconnect.model.CanteenOrder;
import com.mvsr.mvsrconnect.repository.CanteenOrderRepository;
import com.mvsr.mvsrconnect.repository.FoodItemRepository;
import com.mvsr.mvsrconnect.repository.FoodStallRepository;
import com.mvsr.mvsrconnect.service.CanteenService;
import com.mvsr.mvsrconnect.service.VendorSseService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/canteen/vendor")
public class VendorController {

    @Autowired private CanteenService canteenService;
    @Autowired private CanteenOrderRepository orderRepo;
    @Autowired private FoodItemRepository foodItemRepo;
    @Autowired private FoodStallRepository stallRepo;
    @Autowired private VendorSseService sseService;
    @Autowired private Cloudinary cloudinary;

    @Value("${canteen.vendor.session.hours:12}")
    private int sessionHours;

    // ── Login ─────────────────────────────────────────────────────────────

    /** POST /canteen/vendor/login — validates token, sets cookie */
    @GetMapping("/login")
    public void login(@RequestParam String token, HttpServletResponse res) throws Exception {
        try {
            String sessionToken = canteenService.vendorLogin(token, sessionHours);

            Cookie cookie = new Cookie("vendorSession", sessionToken);
            cookie.setHttpOnly(true);
            cookie.setPath("/canteen/vendor");
            cookie.setMaxAge(sessionHours * 3600);

            res.addCookie(cookie);
            res.sendRedirect("/canteen-vendor.html");

        } catch (Exception e) {
            res.sendRedirect("/canteen-vendor-login.html?error=invalid");
        }
    }

    // ── Orders ────────────────────────────────────────────────────────────

    @GetMapping("/api/orders")
    public ResponseEntity<?> activeOrders(HttpServletRequest req) {
        Long stallId = getStallId(req);
        List<String> activeStatuses = List.of("PAID", "PREPARING", "PENDING_VENDOR_VERIFY");
        return ResponseEntity.ok(
                orderRepo.findByStallIdAndStatusIn(stallId, activeStatuses)
                        .stream().map(canteenService::buildOrderSummary).toList()
        );
    }

    @PostMapping("/api/orders/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                          @RequestBody Map<String, String> body,
                                          HttpServletRequest req) {
        try {
            canteenService.updateOrderStatus(id, body.get("status"), getStallId(req));
            return ResponseEntity.ok(Map.of("message", "Status updated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── Menu management ───────────────────────────────────────────────────

    @GetMapping("/api/menu")
    public ResponseEntity<?> getMenu(HttpServletRequest req) {
        return ResponseEntity.ok(foodItemRepo.findByStallId(getStallId(req)));
    }

    @PostMapping("/api/menu")
    public ResponseEntity<?> addItem(@RequestBody Map<String, Object> body,
                                     HttpServletRequest req) {
        try {
            Long stallId = getStallId(req);

            String name = (String) body.get("name");
            BigDecimal price = new BigDecimal(body.get("price").toString());
            String category = (String) body.getOrDefault("category", "");
            String quantityType = (String) body.getOrDefault("quantityType", "TOGGLE");

            var item = new com.mvsr.mvsrconnect.model.FoodItem();
            item.setName(name);
            item.setPrice(price);
            item.setCategory(category);
            item.setStallId(stallId);
            item.setAvailable(true);

            // default behavior
            item.setQuantityType(
                    com.mvsr.mvsrconnect.model.QuantityType.valueOf(quantityType)
            );
            item.setImageUrl((String) body.getOrDefault("imageUrl", null));
            foodItemRepo.save(item);


            return ResponseEntity.ok(Map.of("message", "Item added"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    @DeleteMapping("/api/menu/{itemId}")
    public ResponseEntity<?> deleteItem(@PathVariable Long itemId,
                                        HttpServletRequest req) {
        try {
            Long stallId = getStallId(req);

            var item = foodItemRepo.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("Item not found"));

            if (!item.getStallId().equals(stallId)) {
                throw new RuntimeException("Not your item");
            }

            foodItemRepo.delete(item);

            return ResponseEntity.ok(Map.of("message", "Item deleted"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    @PostMapping(value = "/api/menu/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadMenuImage(@RequestParam("file") MultipartFile file,
                                             HttpServletRequest req) {
        try {
            getStallId(req); // auth check
            if (file.isEmpty() || !file.getContentType().startsWith("image/"))
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid file"));

            var result = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("folder", "canteen", "resource_type", "image",
                            "quality", "auto", "fetch_format", "auto"));

            return ResponseEntity.ok(Map.of(
                    "url",      result.get("secure_url"),
                    "publicId", result.get("public_id")
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/api/menu/{itemId}")
    public ResponseEntity<?> updateItem(@PathVariable Long itemId,
                                        @RequestBody Map<String, Object> body,
                                        HttpServletRequest req) {
        try {
            Long stallId = getStallId(req);

            var item = foodItemRepo.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("Item not found"));

            if (!item.getStallId().equals(stallId)) {
                throw new RuntimeException("Not your item");
            }

            item.setName((String) body.get("name"));
            item.setPrice(new BigDecimal(body.get("price").toString()));
            item.setCategory((String) body.getOrDefault("category", ""));

            foodItemRepo.save(item);

            return ResponseEntity.ok(Map.of("message", "Item updated"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/menu/{itemId}/toggle")
    public ResponseEntity<?> toggleAvailability(@PathVariable Long itemId,
                                                 @RequestBody Map<String, Boolean> body,
                                                 HttpServletRequest req) {
        try {
            canteenService.toggleItem(itemId, body.get("available"), getStallId(req));
            return ResponseEntity.ok(Map.of("message", "Updated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/menu/{itemId}/stock")
    public ResponseEntity<?> setStock(@PathVariable Long itemId,
                                      @RequestBody Map<String, Integer> body,
                                      HttpServletRequest req) {
        try {
            canteenService.setStock(itemId, body.get("count"), getStallId(req));
            return ResponseEntity.ok(Map.of("message", "Stock updated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── SSE ───────────────────────────────────────────────────────────────

    @GetMapping(value = "/api/sse", produces = "text/event-stream")
    public SseEmitter vendorSse(HttpServletRequest req) {
        return sseService.registerVendor(getStallId(req));
    }

    //-----Scanner--------------------------------------------------------------

    @PostMapping("/api/scan")
    public ResponseEntity<?> scanOrder(@RequestBody Map<String, String> body,
                                       HttpServletRequest req) {
        try {
            Long stallId = getStallId(req);
            String code = body.get("code");
            if (code == null || !code.contains("."))
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid QR code format"));

            Long orderId = Long.parseLong(code.split("\\.")[0]);

            CanteenOrder order = orderRepo.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found"));

            // Must belong to this vendor's stall
            if (!order.getStallId().equals(stallId))
                return ResponseEntity.status(403).body(Map.of("error", "Order does not belong to your stall"));

            // Verify HMAC
            String expected = canteenService.generateVerificationCode(order);
            if (!expected.equals(code))
                return ResponseEntity.status(403).body(Map.of("error", "Invalid QR code"));

            // Must be in READY state
            if (!"READY".equals(order.getStatus()))
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Order not ready for pickup",
                        "status", order.getStatus()
                ));

            canteenService.updateOrderStatus(orderId, "PICKED_UP", stallId);
            return ResponseEntity.ok(canteenService.buildOrderSummary(order));

        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid QR code"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private Long getStallId(HttpServletRequest req) {
        Object attr = req.getAttribute("vendorStallId");
        if (attr == null) throw new IllegalStateException("Not authenticated as vendor");
        return (Long) attr;
    }

    @GetMapping("/api/stall-info")
    public ResponseEntity<?> stallInfo(HttpServletRequest req) {
        Long stallId = getStallId(req);
        return stallRepo.findById(stallId)
                .map(s -> ResponseEntity.ok(Map.of("name", s.getName(), "id", stallId)))
                .orElse(ResponseEntity.notFound().build());
    }
}
