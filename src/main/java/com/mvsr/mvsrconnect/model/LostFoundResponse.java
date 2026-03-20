package com.mvsr.mvsrconnect.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lost_found_responses")
@Getter @Setter
@NoArgsConstructor
public class LostFoundResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long itemId;

    private String authorName;
    private Long authorId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    private String contact;
    private String mode; // "found_it" or "its_mine"

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}