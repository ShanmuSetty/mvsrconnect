package com.mvsr.mvsrconnect.controller;

import com.mvsr.mvsrconnect.model.CanteenOrder;
import com.mvsr.mvsrconnect.model.FoodItem;
import com.mvsr.mvsrconnect.model.User;
import com.mvsr.mvsrconnect.repository.CanteenOrderRepository;
import com.mvsr.mvsrconnect.repository.FoodItemRepository;
import com.mvsr.mvsrconnect.repository.FoodStallRepository;
import com.mvsr.mvsrconnect.repository.UserRepository;
import com.mvsr.mvsrconnect.service.CanteenService;
import com.mvsr.mvsrconnect.service.VendorSseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/canteen")
public class CanteenController {

    @Autowired private FoodStallRepository stallRepo;
    @Autowired private FoodItemRepository foodItemRepo;
    @Autowired private CanteenOrderRepository orderRepo;
    @Autowired private CanteenService canteenService;
    @Autowired private VendorSseService sseService;
    @Autowired private UserRepository userRepository;

    @GetMapping("/stalls")
    public ResponseEntity<?> listStalls() {
        return ResponseEntity.ok(stallRepo.findByActiveTrue());
    }

    @GetMapping("/stalls/{stallId}/menu")
    public ResponseEntity<?> stallMenu(@PathVariable Long stallId) {
        List<FoodItem> items = foodItemRepo.findByStallId(stallId);
        return ResponseEntity.ok(items);
    }

    @PostMapping("/order")
    public ResponseEntity<?> placeOrder(@RequestBody Map<String, Object> body,
                                        @AuthenticationPrincipal OAuth2User principal) {
        try {
            Long studentId = getUserId(principal);
            String name = principal.getAttribute("name");
            String email = principal.getAttribute("email");
            Long stallId = Long.valueOf(body.get("stallId").toString());

            List<Map<String, Object>> rawItems = (List<Map<String, Object>>) body.get("items");
            List<CanteenService.CartItem> cart = rawItems.stream()
                    .map(i -> new CanteenService.CartItem(
                            Long.valueOf(i.get("foodItemId").toString()),
                            Integer.parseInt(i.get("quantity").toString())
                    )).toList();

            Map<String, Object> result = canteenService.placeOrder(studentId, stallId, cart, name, email);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/order/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id,
                                      @AuthenticationPrincipal OAuth2User principal) {
        return orderRepo.findById(id)
                .filter(o -> o.getStudentUserId().equals(getUserId(principal)))
                .map(o -> ResponseEntity.ok(canteenService.buildOrderSummary(o)))
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/order/{id}/payment-callback")
    public void paymentCallback(
            @PathVariable Long id,
            @RequestParam(value = "razorpay_payment_id", defaultValue = "") String paymentId,
            @RequestParam(value = "razorpay_payment_link_id", defaultValue = "") String linkId,
            @RequestParam(value = "razorpay_payment_link_reference_id", defaultValue = "") String referenceId,
            @RequestParam(value = "razorpay_payment_link_status", defaultValue = "") String status,
            @RequestParam(value = "razorpay_signature", defaultValue = "") String signature,
            jakarta.servlet.http.HttpServletResponse response) throws Exception {
        try {
            canteenService.verifyAndMarkPaid(id, linkId, referenceId, status, paymentId, signature);
            response.sendRedirect("/canteen.html?order=" + id + "&paid=true");
        } catch (Exception e) {
            response.sendRedirect("/canteen.html?order=" + id + "&paid=false");
        }
    }

    @PostMapping("/order/{id}/utr")
    public ResponseEntity<?> submitUtr(@PathVariable Long id,
                                       @RequestBody Map<String, String> body,
                                       @AuthenticationPrincipal OAuth2User principal) {
        try {
            canteenService.submitUtr(id, getUserId(principal), body.get("utr"));
            return ResponseEntity.ok(Map.of("message", "UTR submitted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my-orders")
    public ResponseEntity<?> myOrders(@AuthenticationPrincipal OAuth2User principal) {
        List<CanteenOrder> orders = orderRepo.findByStudentUserId(getUserId(principal));
        return ResponseEntity.ok(orders.stream().map(canteenService::buildOrderSummary).toList());
    }

    @GetMapping(value = "/order/{id}/sse", produces = "text/event-stream")
    public SseEmitter orderSse(@PathVariable Long id) {
        return sseService.registerOrder(id);
    }

        private Long getUserId(OAuth2User principal) {
            String email = principal.getAttribute("email");

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return user.getId();
        }

}
