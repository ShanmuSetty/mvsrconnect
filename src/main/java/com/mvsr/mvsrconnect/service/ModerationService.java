package com.mvsr.mvsrconnect.service;

import com.cloudinary.Cloudinary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ModerationService {

    private static final Logger log = LoggerFactory.getLogger(ModerationService.class);
    private final RestTemplate rest = new RestTemplate();
    private final Cloudinary cloudinary;

    @Value("${moderation.url:http://localhost:5001}")
    private String moderationUrl;

    @Value("${moderation.enabled:true}")
    private boolean moderationEnabled;

    public ModerationService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    // ── TEXT MODERATION via Flask ──
    public boolean isTextToxic(String text, String context) {
        if (!moderationEnabled) return false;
        try {
            Map<String, String> req = Map.of(
                    "text", text,
                    "context", context == null ? "" : context
            );
            Map res = rest.postForObject(moderationUrl + "/check_text", req, Map.class);
            return (boolean) res.get("toxic");
        } catch (Exception e) {
            log.warn("Text moderation failed, skipping: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTextToxic(String text) {
        return isTextToxic(text, "");
    }

    // ── IMAGE MODERATION via Flask ──
    public boolean isImageUnsafe(String url) {
        if (!moderationEnabled) return false;
        try {
            Map<String, String> req = Map.of("url", url);
            Map res = rest.postForObject(moderationUrl + "/check_image", req, Map.class);
            return (boolean) res.get("unsafe");
        } catch (Exception e) {
            log.warn("Image moderation failed, skipping: {}", e.getMessage());
            return false;
        }
    }

    // ── VIDEO MODERATION via Flask ──
    public boolean isVideoUnsafe(String url) {
        if (!moderationEnabled) return false;
        try {
            Map<String, String> req = Map.of("url", url);
            Map res = rest.postForObject(moderationUrl + "/check_video", req, Map.class);
            return (boolean) res.get("unsafe");
        } catch (Exception e) {
            log.warn("Video moderation failed, skipping: {}", e.getMessage());
            return false;
        }
    }
}