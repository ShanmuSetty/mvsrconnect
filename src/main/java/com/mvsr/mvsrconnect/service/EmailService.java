package com.mvsr.mvsrconnect.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(to);
            mail.setSubject(subject);
            mail.setText(body);

            mailSender.send(mail);

            System.out.println("EMAIL SENT → " + to);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Async
    public void sendJoinApproval(String to, String name, String club) {
        sendEmail(
                to,
                "You've been approved 🎉",
                "Hi " + name + ",\n\n" +
                        "Your request to join \"" + club + "\" has been approved.\n\n" +
                        "You can now access club content.\n\n" +
                        "— MvsrConnect Team"
        );
    }

    @Async
    public void sendJoinRejection(String to, String name, String club) {
        sendEmail(
                to,
                "Join Request Rejected",
                "Hi " + name + ",\n\n" +
                        "Your request to join \"" + club + "\" was not approved.\n\n" +
                        "You can try again later.\n\n" +
                        "— MvsrConnect Team"
        );
    }

    @Async
    public void sendModeratorApproval(String to, String name, String club) {
        sendEmail(
                to,
                "You're now a Moderator 🎉",
                "Hi " + name + ",\n\n" +
                        "Your request to moderate \"" + club + "\" has been approved.\n\n" +
                        "You now have moderation privileges.\n\n" +
                        "— MvsrConnect Team"
        );
    }

    @Async
    public void sendModeratorRejection(String to, String name, String club) {
        sendEmail(
                to,
                "Moderator Request Update",
                "Hi " + name + ",\n\n" +
                        "Your request to moderate \"" + club + "\" was not approved.\n\n" +
                        "You can try again later.\n\n" +
                        "— MvsrConnect Team"
        );
    }
}