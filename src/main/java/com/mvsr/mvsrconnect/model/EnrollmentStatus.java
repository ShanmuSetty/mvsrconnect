package com.mvsr.mvsrconnect.model;

public enum EnrollmentStatus {
    PENDING_PAYMENT,    // free events skip this — never used for free events
    PENDING_APPROVAL,   // student paid and submitted UTR, waiting for manager
    CONFIRMED,          // manager approved → QR ticket issued
    REJECTED,           // manager rejected payment
    CHECKED_IN          // scanned at entry
}
