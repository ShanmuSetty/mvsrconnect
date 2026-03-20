package com.mvsr.mvsrconnect.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.mvsr.mvsrconnect.dto.LostFoundItemDTO;
import com.mvsr.mvsrconnect.model.LostFoundItem;
import com.mvsr.mvsrconnect.model.LostFoundResponse;
import com.mvsr.mvsrconnect.model.User;
import com.mvsr.mvsrconnect.repository.LostFoundItemRepository;
import com.mvsr.mvsrconnect.repository.LostFoundResponseRepository;
import com.mvsr.mvsrconnect.repository.UserRepository;
import com.mvsr.mvsrconnect.service.ModerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/lost-found")
public class LostFoundController {

    @Autowired private LostFoundItemRepository itemRepo;
    @Autowired private LostFoundResponseRepository responseRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private ModerationService moderationService;
    @Autowired private Cloudinary cloudinary;

    // ── GET /lost-found ──────────────────────────────
    @GetMapping
    public ResponseEntity<List<LostFoundItemDTO>> getAllItems() {
        List<LostFoundItem> items = itemRepo.findAllByOrderByCreatedAtDesc();
        List<LostFoundItemDTO> dtos = items.stream()
                .map(item -> LostFoundItemDTO.from(item, responseRepo.countByItemId(item.getId())))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    // ── GET /lost-found/search?q= ────────────────────
    @GetMapping("/search")
    public ResponseEntity<List<LostFoundItemDTO>> search(@RequestParam String q) {
        if (q == null || q.trim().isEmpty()) return getAllItems();
        List<LostFoundItem> items = itemRepo.search(q.trim());
        List<LostFoundItemDTO> dtos = items.stream()
                .map(item -> LostFoundItemDTO.from(item, responseRepo.countByItemId(item.getId())))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    // ── POST /lost-found ─────────────────────────────
    @PostMapping
    public ResponseEntity<?> createItem(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal OAuth2User principal) {

        if (principal == null) return ResponseEntity.status(401).body("Not authenticated");

        String title   = body.getOrDefault("title", "").trim();
        String typeStr = body.getOrDefault("type", "").trim().toUpperCase();

        if (title.isEmpty()) return ResponseEntity.badRequest().body("Title is required");
        if (!typeStr.equals("LOST") && !typeStr.equals("FOUND"))
            return ResponseEntity.badRequest().body("Type must be LOST or FOUND");

        String description = body.getOrDefault("description", "").trim();
        String mediaUrl    = body.getOrDefault("mediaUrl", "").trim();
        String mediaType   = body.getOrDefault("mediaType", "").trim();
        String mediaPublicId = body.getOrDefault("mediaPublicId", "").trim();

        // Text moderation
        if (!description.isEmpty()) {
            try {
                if (moderationService.isTextToxic(title + " " + description))
                    return ResponseEntity.badRequest().body("Content flagged by moderation");
            } catch (Exception ignored) {}
        }

        // Image moderation
        if (!mediaUrl.isEmpty() && "image".equals(mediaType)) {
            try {
                if (moderationService.isImageUnsafe(mediaUrl)) {
                    deleteFromCloudinary(mediaPublicId, "image");
                    return ResponseEntity.badRequest().body("Unsafe image detected");
                }
            } catch (Exception ignored) {}
        }

        String email = principal.getAttribute("email");
        User user = userRepo.findByEmail(email).orElse(null);

        LostFoundItem item = new LostFoundItem();
        item.setTitle(title);
        item.setType(LostFoundItem.ItemType.valueOf(typeStr));
        item.setDescription(description);
        item.setLocation(body.getOrDefault("location", "").trim());
        item.setCategory(body.getOrDefault("category", "").trim());
        item.setDate(body.getOrDefault("date", "").trim());
        item.setAuthorName(user != null ? user.getName() : principal.getAttribute("name"));
        item.setAuthorId(user != null ? user.getId() : null);
        if (!mediaUrl.isEmpty())      item.setMediaUrl(mediaUrl);
        if (!mediaType.isEmpty())     item.setMediaType(mediaType);
        if (!mediaPublicId.isEmpty()) item.setMediaPublicId(mediaPublicId);

        LostFoundItem saved = itemRepo.save(item);
        return ResponseEntity.ok(LostFoundItemDTO.from(saved, 0));
    }

    // ── DELETE /lost-found/{id} ──────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(
            @PathVariable Long id,
            @AuthenticationPrincipal OAuth2User principal) {

        if (principal == null) return ResponseEntity.status(401).body("Not authenticated");

        Optional<LostFoundItem> opt = itemRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        LostFoundItem item = opt.get();
        String email = principal.getAttribute("email");
        User user = userRepo.findByEmail(email).orElse(null);

        boolean isAdmin  = user != null && "ADMIN".equals(user.getRole() != null ? user.getRole().name() : "");
        boolean isAuthor = user != null && user.getId().equals(item.getAuthorId());

        if (!isAdmin && !isAuthor) return ResponseEntity.status(403).body("Not allowed");

        // Clean up Cloudinary
        if (item.getMediaPublicId() != null && !item.getMediaPublicId().isEmpty()) {
            deleteFromCloudinary(item.getMediaPublicId(), item.getMediaType());
        }

        responseRepo.findByItemIdOrderByCreatedAtAsc(id)
                .forEach(r -> responseRepo.deleteById(r.getId()));
        itemRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ── POST /lost-found/{id}/resolve ────────────────
    @PostMapping("/{id}/resolve")
    public ResponseEntity<?> resolveItem(
            @PathVariable Long id,
            @AuthenticationPrincipal OAuth2User principal) {

        if (principal == null) return ResponseEntity.status(401).body("Not authenticated");

        Optional<LostFoundItem> opt = itemRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        LostFoundItem item = opt.get();
        String email = principal.getAttribute("email");
        User user = userRepo.findByEmail(email).orElse(null);

        boolean isAdmin  = user != null && "ADMIN".equals(user.getRole() != null ? user.getRole().name() : "");
        boolean isAuthor = user != null && user.getId().equals(item.getAuthorId());

        if (!isAdmin && !isAuthor) return ResponseEntity.status(403).body("Not allowed");

        item.setResolved(true);
        itemRepo.save(item);
        return ResponseEntity.ok(LostFoundItemDTO.from(item, responseRepo.countByItemId(id)));
    }

    // ── GET /lost-found/{id}/responses ───────────────
    @GetMapping("/{id}/responses")
    public ResponseEntity<?> getResponses(
            @PathVariable Long id,
            @AuthenticationPrincipal OAuth2User principal) {

        if (principal == null) return ResponseEntity.status(401).body("Not authenticated");

        Optional<LostFoundItem> opt = itemRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        LostFoundItem item = opt.get();
        String email = principal.getAttribute("email");
        User user = userRepo.findByEmail(email).orElse(null);

        boolean isAdmin  = user != null && "ADMIN".equals(user.getRole() != null ? user.getRole().name() : "");
        boolean isAuthor = user != null && user.getId().equals(item.getAuthorId());

        if (!isAdmin && !isAuthor) return ResponseEntity.status(403).body("Only the poster can view responses");

        return ResponseEntity.ok(responseRepo.findByItemIdOrderByCreatedAtAsc(id));
    }

    // ── POST /lost-found/{id}/respond ────────────────
    @PostMapping("/{id}/respond")
    public ResponseEntity<?> respond(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal OAuth2User principal) {

        if (principal == null) return ResponseEntity.status(401).body("Not authenticated");

        Optional<LostFoundItem> opt = itemRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        LostFoundItem item = opt.get();
        if (item.isResolved()) return ResponseEntity.badRequest().body("This item is already resolved");

        String message = body.getOrDefault("message", "").trim();
        if (message.isEmpty()) return ResponseEntity.badRequest().body("Message is required");

        try {
            if (moderationService.isTextToxic(message))
                return ResponseEntity.badRequest().body("Message flagged by moderation");
        } catch (Exception ignored) {}

        String email = principal.getAttribute("email");
        User user = userRepo.findByEmail(email).orElse(null);

        if (user != null && user.getId().equals(item.getAuthorId()))
            return ResponseEntity.badRequest().body("You cannot respond to your own post");

        if (user != null && responseRepo.existsByItemIdAndAuthorId(id, user.getId()))
            return ResponseEntity.badRequest().body("You have already responded to this item");

        LostFoundResponse response = new LostFoundResponse();
        response.setItemId(id);
        response.setMessage(message);
        response.setContact(body.getOrDefault("contact", "").trim());
        response.setMode(body.getOrDefault("mode", "").trim());
        response.setAuthorName(user != null ? user.getName() : principal.getAttribute("name"));
        response.setAuthorId(user != null ? user.getId() : null);

        return ResponseEntity.ok(responseRepo.save(response));
    }

    // ── CLOUDINARY HELPER ────────────────────────────
    private void deleteFromCloudinary(String publicId, String mediaType) {
        try {
            String resourceType = "video".equals(mediaType) ? "video" : "image";
            cloudinary.uploader().destroy(publicId,
                    ObjectUtils.asMap("resource_type", resourceType, "invalidate", true));
        } catch (Exception e) {
            System.out.println("Cloudinary delete failed: " + e.getMessage());
        }
    }
}