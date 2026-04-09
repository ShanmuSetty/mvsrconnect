package com.mvsr.mvsrconnect.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name="food_items")
@Getter
@Setter
public class FoodItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private BigDecimal price;

    private boolean available;
    private Long stallId;

    @Enumerated(EnumType.STRING)
    private QuantityType quantityType = QuantityType.TOGGLE;

    private int stockCount = 0;

    private String imageUrl;
    private String category;
}