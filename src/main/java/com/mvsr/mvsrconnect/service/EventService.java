package com.mvsr.mvsrconnect.service;

import com.mvsr.mvsrconnect.model.*;
import com.mvsr.mvsrconnect.repository.EventEnrollmentRepository;
import com.mvsr.mvsrconnect.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final EventEnrollmentRepository enrollmentRepository;

    public EventService(EventRepository eventRepository,
                        EventEnrollmentRepository enrollmentRepository) {
        this.eventRepository = eventRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    // ─── Event CRUD ────────────────────────────────────────────────────────────

    public Event createEvent(Event event) {
        event.setCreatedAt(LocalDateTime.now());
        event.setActive(true);
        return eventRepository.save(event);
    }

    public List<Event> getActiveEvents() {
        return eventRepository.findByActiveTrueOrderByEventDateAsc();
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id).orElseThrow();
    }

    public List<Event> getMyEvents(Long organizerId) {
        return eventRepository.findByOrganizerIdOrderByCreatedAtDesc(organizerId);
    }

    public void cancelEvent(Long id) {
        Event event = eventRepository.findById(id).orElseThrow();
        event.setActive(false);
        eventRepository.save(event);
    }

    // ─── Safe public view (strips UPI details) ─────────────────────────────────
    // UPI ID is sensitive — organizer's bank. Never expose it in list/detail APIs.

    public Map<String, Object> toPublicView(Event event) {
        Map<String, Object> map = new HashMap<>();
        map.put("id",            event.getId());
        map.put("title",         event.getTitle());
        map.put("description",   event.getDescription());
        map.put("venue",         event.getVenue());
        map.put("eventDate",     event.getEventDate());
        map.put("feeInPaise",    event.getFeeInPaise());
        map.put("capacity",      event.getCapacity());
        map.put("organizerId",   event.getOrganizerId());
        map.put("organizerName", event.getOrganizerName());
        map.put("clubId",        event.getClubId());
        map.put("bannerUrl",     event.getBannerUrl());
        map.put("active",        event.isActive());
        map.put("createdAt",     event.getCreatedAt());
        map.put("isFree",        event.getFeeInPaise() == 0);
        // confirmed enrollment count for display
        int confirmed = enrollmentRepository.countByEventIdAndStatus(event.getId(), EnrollmentStatus.CONFIRMED)
                      + enrollmentRepository.countByEventIdAndStatus(event.getId(), EnrollmentStatus.CHECKED_IN);
        map.put("enrolledCount", confirmed);
        return map;
    }

    // ─── Enrollment ─────────────────────────────────────────────────────────────

    public EventEnrollment enroll(Long eventId, User user) {
        Event event = eventRepository.findById(eventId).orElseThrow();

        if (!event.isActive()) {
            throw new IllegalStateException("Event is no longer active.");
        }
        if (enrollmentRepository.existsByEventIdAndUserId(eventId, user.getId())) {
            throw new IllegalStateException("You are already enrolled in this event.");
        }

        // capacity check (count confirmed + checked_in + pending_approval)
        if (event.getCapacity() != null) {
            int taken = enrollmentRepository.countByEventIdAndStatus(eventId, EnrollmentStatus.CONFIRMED)
                      + enrollmentRepository.countByEventIdAndStatus(eventId, EnrollmentStatus.CHECKED_IN)
                      + enrollmentRepository.countByEventIdAndStatus(eventId, EnrollmentStatus.PENDING_APPROVAL);
            if (taken >= event.getCapacity()) {
                throw new IllegalStateException("Event is full.");
            }
        }

        EventEnrollment enrollment = new EventEnrollment();
        enrollment.setEventId(eventId);
        enrollment.setUserId(user.getId());
        enrollment.setUserName(user.getName());
        enrollment.setUserEmail(user.getEmail());
        enrollment.setUserPicture(user.getPicture());
        enrollment.setEnrolledAt(LocalDateTime.now());

        if (event.getFeeInPaise() == 0) {
            // free event → confirm immediately and issue QR
            enrollment.setStatus(EnrollmentStatus.CONFIRMED);
            enrollment.setQrToken(UUID.randomUUID().toString());
        } else {
            // paid event → student needs to pay and submit UTR
            enrollment.setStatus(EnrollmentStatus.PENDING_PAYMENT);
        }

        return enrollmentRepository.save(enrollment);
    }

    // ─── Payment submission (student submits UTR after paying) ─────────────────

    public EventEnrollment submitPayment(Long eventId, Long userId, String utrNumber) {
        EventEnrollment enrollment = enrollmentRepository
                .findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new IllegalStateException("Enrollment not found."));

        if (enrollment.getStatus() != EnrollmentStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Payment already submitted or enrollment not in payment-pending state.");
        }
        if (utrNumber == null || utrNumber.isBlank()) {
            throw new IllegalArgumentException("UTR number is required.");
        }

        enrollment.setUtrNumber(utrNumber.trim());
        enrollment.setStatus(EnrollmentStatus.PENDING_APPROVAL);
        return enrollmentRepository.save(enrollment);
    }

    // ─── Manager: approve payment → issue QR ───────────────────────────────────

    public EventEnrollment approveEnrollment(Long enrollmentId, Long managerId) {
        EventEnrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow();
        Event event = eventRepository.findById(enrollment.getEventId()).orElseThrow();

        if (!event.getOrganizerId().equals(managerId)) {
            throw new SecurityException("Not authorized to manage this event.");
        }
        if (enrollment.getStatus() != EnrollmentStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Enrollment is not pending approval.");
        }

        enrollment.setStatus(EnrollmentStatus.CONFIRMED);
        enrollment.setQrToken(UUID.randomUUID().toString());
        return enrollmentRepository.save(enrollment);
    }

    // ─── Manager: reject payment ────────────────────────────────────────────────

    public EventEnrollment rejectEnrollment(Long enrollmentId, Long managerId) {
        EventEnrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow();
        Event event = eventRepository.findById(enrollment.getEventId()).orElseThrow();

        if (!event.getOrganizerId().equals(managerId)) {
            throw new SecurityException("Not authorized to manage this event.");
        }

        enrollment.setStatus(EnrollmentStatus.REJECTED);
        return enrollmentRepository.save(enrollment);
    }

    // ─── Scanner: verify QR token ───────────────────────────────────────────────

    public Map<String, Object> verifyQr(String qrToken) {
        EventEnrollment enrollment = enrollmentRepository.findByQrToken(qrToken)
                .orElseThrow(() -> new IllegalStateException("Invalid QR code."));

        Event event = eventRepository.findById(enrollment.getEventId()).orElseThrow();

        Map<String, Object> result = new HashMap<>();
        result.put("enrollmentId", enrollment.getId());
        result.put("userName",     enrollment.getUserName());
        result.put("userEmail",    enrollment.getUserEmail());
        result.put("userPicture",  enrollment.getUserPicture());
        result.put("eventTitle",   event.getTitle());
        result.put("eventVenue",   event.getVenue());
        result.put("eventDate",    event.getEventDate());
        result.put("status",       enrollment.getStatus());
        result.put("alreadyIn",    enrollment.getStatus() == EnrollmentStatus.CHECKED_IN);
        result.put("checkedInAt",  enrollment.getCheckedInAt());
        return result;
    }

    // ─── Scanner: mark checked in ───────────────────────────────────────────────

    public Map<String, Object> checkIn(String qrToken, Long scannerId) {
        EventEnrollment enrollment = enrollmentRepository.findByQrToken(qrToken)
                .orElseThrow(() -> new IllegalStateException("Invalid QR code."));

        Event event = eventRepository.findById(enrollment.getEventId()).orElseThrow();

        // only the organizer (or admins — handled in controller) can check in
        if (!event.getOrganizerId().equals(scannerId)) {
            throw new SecurityException("Not authorized to check in for this event.");
        }
        if (enrollment.getStatus() == EnrollmentStatus.CHECKED_IN) {
            throw new IllegalStateException("Already checked in.");
        }
        if (enrollment.getStatus() != EnrollmentStatus.CONFIRMED) {
            throw new IllegalStateException("Enrollment is not confirmed.");
        }

        enrollment.setStatus(EnrollmentStatus.CHECKED_IN);
        enrollment.setCheckedInAt(LocalDateTime.now());
        enrollmentRepository.save(enrollment);

        Map<String, Object> result = new HashMap<>();
        result.put("success",    true);
        result.put("userName",   enrollment.getUserName());
        result.put("eventTitle", event.getTitle());
        result.put("checkedInAt", enrollment.getCheckedInAt());
        return result;
    }

    // ─── UPI deeplink builder ───────────────────────────────────────────────────
    // Returns the upi:// deeplink for a given event.
    // Called server-side only for enrolled users — upiId never goes to the browser raw.

    public String buildUpiDeeplink(Event event, Long enrollmentId) {
        double amount = event.getFeeInPaise() / 100.0;
        String note = "MVSRCONNECT-" + enrollmentId;
        return String.format(
            "upi://pay?pa=%s&pn=%s&am=%.2f&cu=INR&tn=%s",
            event.getUpiId(),
            encode(event.getUpiName()),
            amount,
            note
        );
    }

    private String encode(String s) {
        try {
            return java.net.URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            return s;
        }
    }

    // ─── Manager dashboard helpers ──────────────────────────────────────────────

    public List<EventEnrollment> getEnrollmentsForEvent(Long eventId, Long requesterId) {
        Event event = eventRepository.findById(eventId).orElseThrow();
        if (!event.getOrganizerId().equals(requesterId)) {
            throw new SecurityException("Not authorized.");
        }
        return enrollmentRepository.findByEventIdOrderByEnrolledAtAsc(eventId);
    }

    public List<EventEnrollment> getMyTickets(Long userId) {
        return enrollmentRepository.findByUserIdOrderByEnrolledAtDesc(userId);
    }
}
