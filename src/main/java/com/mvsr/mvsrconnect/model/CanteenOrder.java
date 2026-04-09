package com.mvsr.mvsrconnect.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class CanteenOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentUserId;
    private Long stallId;
    private BigDecimal totalAmount;

    // PENDING_PAYMENT, PAID, PREPARING, READY, PICKED_UP, CANCELLED
    private String status = "PENDING_PAYMENT";
    private String paymentMode = "AUTO"; // AUTO | MANUAL
    private String razorpayPaymentLinkId;
    private String razorpayPaymentId;
    private String utrNumber;
    private Integer tokenNumber;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItem> items;
}
