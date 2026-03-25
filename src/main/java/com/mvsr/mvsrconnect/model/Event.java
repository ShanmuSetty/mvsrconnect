package com.mvsr.mvsrconnect.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private String venue;

    @Column(nullable = false)
    private LocalDateTime eventDate;

    // 0 = free event, anything above = paid (stored in paise, e.g. 15000 = ₹150)
    @Column(nullable = false)
    private int feeInPaise = 0;

    // null = unlimited
    private Integer capacity;

    // UPI details — NEVER returned in any API response (see EventController)
    @Column(name = "upi_id")
    private String upiId;

    @Column(name = "upi_name")
    private String upiName;

    // organizer
    @Column(nullable = false)
    private Long organizerId;

    @Column(nullable = false)
    private String organizerName;

    // optional — if event is tied to a club
    private Long clubId;

    // poster image
    @Column(length = 512)
    private String bannerUrl;

    private String bannerPublicId;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
