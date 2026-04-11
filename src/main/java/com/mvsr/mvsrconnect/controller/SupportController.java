package com.mvsr.mvsrconnect.controller;

import com.mvsr.mvsrconnect.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/support")
public class SupportController {

    @Autowired
    private EmailService emailService;

    @Value("${MAIL_USERNAME:}")
    private String adminEmail;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> submitReport(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String category,
            @RequestParam String subject,
            @RequestParam String description,
            @RequestParam(defaultValue = "low") String priority,
            @RequestParam(required = false) MultipartFile attachment
    ) {
        try {
            String attachmentInfo = "";
            if (attachment != null && !attachment.isEmpty()) {
                attachmentInfo = "\n\nAttachment: " + attachment.getOriginalFilename()
                        + " (" + (attachment.getSize() / 1024) + " KB)";
                // TODO: optionally upload to Cloudinary and include link
            }

            String body = "New support request from MVSR Connect\n"
                    + "=".repeat(40) + "\n\n"
                    + "From:        " + name + " <" + email + ">\n"
                    + "Category:    " + category + "\n"
                    + "Priority:    " + priority.toUpperCase() + "\n\n"
                    + "Description:\n" + description
                    + attachmentInfo
                    + "\n\n" + "=".repeat(40)
                    + "\nReply directly to this email to respond to the student.";

            String mailTarget = (adminEmail != null && !adminEmail.isBlank())
                    ? adminEmail
                    : "shanmukhavarshith@mvsrec.edu.in";

            emailService.sendEmail(
                    mailTarget,
                    "[MVSR Connect Support] [" + priority.toUpperCase() + "] " + subject,
                    body
            );

            // Also send an acknowledgement to the student
            emailService.sendEmail(
                    email,
                    "We got your report — MVSR Connect Support",
                    "Hi " + name + ",\n\n"
                            + "Thanks for reaching out! We've received your report about:\n\n"
                            + "  " + subject + "\n\n"
                            + "We'll look into it and get back to you within 24–48 hours.\n\n"
                            + "— MVSR Connect"
            );

            return ResponseEntity.ok(Map.of("message", "Report submitted successfully"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to send report"));
        }
    }
}
