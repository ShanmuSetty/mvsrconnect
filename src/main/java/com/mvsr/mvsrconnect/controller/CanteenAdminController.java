package com.mvsr.mvsrconnect.controller;

import com.mvsr.mvsrconnect.model.FoodItem;
import com.mvsr.mvsrconnect.model.FoodStall;
import com.mvsr.mvsrconnect.model.QuantityType;
import com.mvsr.mvsrconnect.repository.FoodItemRepository;
import com.mvsr.mvsrconnect.repository.FoodStallRepository;
import com.mvsr.mvsrconnect.service.CanteenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/canteen/admin")
@PreAuthorize("hasRole('ADMIN')")
public class CanteenAdminController {

    @Autowired
    private FoodStallRepository stallRepo;
    @Autowired
    private FoodItemRepository foodItemRepo;
    @Autowired
    private CanteenService canteenService;

    @GetMapping("/stalls")
    public ResponseEntity<?> listStalls() {
        return ResponseEntity.ok(stallRepo.findAll());
    }

    @PostMapping("/stalls")
    public ResponseEntity<?> createStall(@RequestBody Map<String, String> body) {
        FoodStall stall = new FoodStall();
        stall.setName(body.get("name"));
        stall.setUpiId(body.get("upiId"));
        stall.setUpiQrUrl(body.get("upiQrUrl"));
        stall.setVendorEmail(body.get("vendorEmail"));
        stall = stallRepo.save(stall);

        String token = canteenService.generateVendorToken(stall.getId());
        return ResponseEntity.ok(Map.of(
                "stallId", stall.getId(),
                "setupToken", token,
                "loginUrl", "/canteen-vendor-login.html?token=" + token,
                "note", "Share this URL with vendor. Token shown only once."
        ));
    }

    @PostMapping("/stalls/{id}/token")
    public ResponseEntity<?> regenerateToken(@PathVariable Long id) {
        String token = canteenService.generateVendorToken(id);
        return ResponseEntity.ok(Map.of(
                "setupToken", token,
                "loginUrl", "/canteen-vendor-login.html?token=" + token
        ));
    }

    @PostMapping("/menu")
    public ResponseEntity<?> addItem(@RequestBody Map<String, Object> body) {
        FoodItem fi = new FoodItem();
        fi.setName(body.get("name").toString());
        fi.setPrice(new BigDecimal(body.get("price").toString()));
        fi.setStallId(Long.valueOf(body.get("stallId").toString()));
        fi.setCategory(body.getOrDefault("category", "").toString());
        fi.setImageUrl(body.getOrDefault("imageUrl", "").toString());
        fi.setQuantityType(QuantityType.valueOf(body.getOrDefault("quantityType", "TOGGLE").toString()));
        fi.setAvailable(true);
        return ResponseEntity.ok(foodItemRepo.save(fi));
    }

    @PutMapping("/menu/{id}")
    public ResponseEntity<?> editItem(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return foodItemRepo.findById(id).map(fi -> {
            if (body.containsKey("name")) fi.setName(body.get("name").toString());
            if (body.containsKey("price")) fi.setPrice(new BigDecimal(body.get("price").toString()));
            if (body.containsKey("category")) fi.setCategory(body.get("category").toString());
            if (body.containsKey("imageUrl")) fi.setImageUrl(body.get("imageUrl").toString());
            if (body.containsKey("quantityType"))
                fi.setQuantityType(QuantityType.valueOf(body.get("quantityType").toString()));
            return ResponseEntity.ok(foodItemRepo.save(fi));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/menu/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable Long id) {
        foodItemRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }
}
