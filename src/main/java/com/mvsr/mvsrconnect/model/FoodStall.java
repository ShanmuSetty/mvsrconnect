package com.mvsr.mvsrconnect.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "food_stalls")
@Getter
@Setter
public class FoodStall {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String upiId;
    private String upiQrUrl;
    private String vendorEmail;
    private String setupTokenHash;
    private boolean tokenUsed = false;
    private boolean active = true;
}
