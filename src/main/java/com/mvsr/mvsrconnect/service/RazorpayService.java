package com.mvsr.mvsrconnect.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Map;

@Service
public class RazorpayService {

    @Value("${razorpay.key.id:}")
    private String keyId;

    @Value("${razorpay.key.secret:}")
    private String keySecret;

    @Value("${razorpay.webhook.secret:}")
    private String webhookSecret;

    @Value("${app.base-url}")
    private String baseUrl;

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Creates a Razorpay Payment Link and returns {id, short_url}.
     * amount is in RUPEES (converted to paise internally).
     */
    public Map<String, String> createPaymentLink(long orderId, double amountRupees,
                                                  String studentName, String studentEmail) throws Exception {
        long amountPaise = Math.round(amountRupees * 100);
        String body = mapper.writeValueAsString(Map.of(
                "amount", amountPaise,
                "currency", "INR",
                "description", "Canteen Order #" + orderId,
                "customer", Map.of("name", studentName, "email", studentEmail),
                "notify", Map.of("sms", false, "email", false),
                "callback_url", baseUrl + "/canteen/order/" + orderId + "/payment-callback",
                "callback_method", "get"
        ));

        String auth = Base64.getEncoder().encodeToString((keyId + ":" + keySecret).getBytes());

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.razorpay.com/v1/payment_links"))
                .header("Authorization", "Basic " + auth)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() != 200 && res.statusCode() != 201) {
            throw new RuntimeException("Razorpay error: " + res.body());
        }

        JsonNode json = mapper.readTree(res.body());

        if (!json.has("id") || !json.has("short_url")) {
            throw new RuntimeException("Invalid Razorpay response: " + json);
        }
        return Map.of(
                "id", json.get("id").asText(),
                "url", json.get("short_url").asText()
        );
    }

    /**
     * Verifies Razorpay webhook signature.
     * Returns true if valid.
     */
    public boolean verifyWebhookSignature(String rawBody, String razorpaySignature) {
        if (webhookSecret == null || webhookSecret.isBlank()) return false;
        String generated = HmacUtils.hmacSha256Hex(webhookSecret, rawBody);
        return generated.equals(razorpaySignature);
    }
    /**
     * Verifies Razorpay payment link redirect signature.
     * Called when user is redirected back after payment.
     */
    public boolean verifyPaymentLinkSignature(String paymentLinkId,
                                              String paymentLinkReferenceId,
                                              String paymentLinkStatus,
                                              String paymentId,
                                              String signature) {
        String payload = paymentLinkId + "|" + paymentLinkReferenceId + "|" + paymentLinkStatus + "|" + paymentId;
        String generated = HmacUtils.hmacSha256Hex(keySecret, payload);
        return generated.equals(signature);
    }

    /**
     * Extracts the payment link ID from a webhook payload.
     * Webhook event: payment_link.paid
     */
    public String extractPaymentLinkId(String rawBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(rawBody);

            String id = root.path("payload")
                    .path("payment_link")
                    .path("entity")
                    .path("id")
                    .asText();

            System.out.println("✅ Extracted linkId: " + id);
            return id;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Extracts the Razorpay payment ID from webhook payload.
     */
    public String extractPaymentId(String rawBody) throws Exception {
        JsonNode json = mapper.readTree(rawBody);
        return json.path("payload").path("payment").path("entity").path("id").asText(null);
    }
}
