package com.mvsr.mvsrconnect.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "vendor_sessions")
@Getter
@Setter
public class VendorSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long stallId;
    private String sessionToken;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt = LocalDateTime.now();
}
