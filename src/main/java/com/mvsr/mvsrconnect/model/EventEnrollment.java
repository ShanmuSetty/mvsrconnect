package com.mvsr.mvsrconnect.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "event_enrollments",
    uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "user_id"})
)
@Getter
@Setter
public class EventEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private String userEmail;

    @Column(length = 512)
    private String userPicture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status;

    // student fills this after paying — e.g. "324512345678"
    private String utrNumber;

    // generated UUID on CONFIRMED, this is what the QR encodes
    @Column(unique = true)
    private String qrToken;

    @Column(nullable = false)
    private LocalDateTime enrolledAt;

    private LocalDateTime checkedInAt;
}
