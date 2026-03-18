package com.mvsr.mvsrconnect.service;

import com.cloudinary.Cloudinary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class ModerationService {

    private static final Logger log = LoggerFactory.getLogger(ModerationService.class);
    private final RestTemplate rest = new RestTemplate();
    private final Cloudinary cloudinary;

    @Value("${openai.api.key:}")
    private String openAiApiKey;

    @Value("${moderation.enabled:true}")
    private boolean moderationEnabled;

    public ModerationService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    // ── TEXT MODERATION via OpenAI ──
    public boolean isTextToxic(String text, String context) {
        if (!moderationEnabled) return false;
        if (openAiApiKey == null || openAiApiKey.isBlank()) {
            log.warn("OpenAI API key not set, skipping text moderation");
            return false;
        }
        try {
            String combined = (context != null && !context.isBlank())
                    ? context + " " + text : text;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiApiKey);

            Map<String, String> body = Map.of("input", combined);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = rest.postForEntity(
                    "https://api.openai.com/v1/moderations",
                    request,
                    Map.class
            );

            List<Map<String, Object>> results =
                    (List<Map<String, Object>>) response.getBody().get("results");

            return (boolean) results.get(0).get("flagged");

        } catch (Exception e) {
            log.warn("Text moderation failed, skipping: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTextToxic(String text) {
        return isTextToxic(text, "");
    }

    // ── IMAGE MODERATION via Cloudinary ──
    public boolean isImageUnsafe(String publicId) {
        if (!moderationEnabled) return false;
        try {
            Map result = cloudinary.api().resource(
                    publicId,
                    com.cloudinary.utils.ObjectUtils.asMap("moderations", "aws_rek")
            );
            Map moderation = extractModerationStatus(result);
            if (moderation == null) return false;
            return "rejected".equalsIgnoreCase((String) moderation.get("status"));
        } catch (Exception e) {
            log.warn("Image moderation failed, skipping: {}", e.getMessage());
            return false;
        }
    }

    // ── VIDEO MODERATION via Cloudinary ──
    public boolean isVideoUnsafe(String publicId) {
        if (!moderationEnabled) return false;
        try {
            Map result = cloudinary.api().resource(
                    publicId,
                    com.cloudinary.utils.ObjectUtils.asMap(
                            "resource_type", "video",
                            "moderations", "aws_rek"
                    )
            );
            Map moderation = extractModerationStatus(result);
            if (moderation == null) return false;
            return "rejected".equalsIgnoreCase((String) moderation.get("status"));
        } catch (Exception e) {
            log.warn("Video moderation failed, skipping: {}", e.getMessage());
            return false;
        }
    }

    private Map extractModerationStatus(Map result) {
        try {
            List<Map> moderationList = (List<Map>) result.get("moderation");
            if (moderationList == null || moderationList.isEmpty()) return null;
            return moderationList.get(0);
        } catch (Exception e) {
            return null;
        }
    }
}