package com.mvsr.mvsrconnect.controller;

import com.mvsr.mvsrconnect.model.*;
import com.mvsr.mvsrconnect.repository.EventEnrollmentRepository;
import com.mvsr.mvsrconnect.repository.EventRepository;
import com.mvsr.mvsrconnect.repository.UserRepository;
import com.mvsr.mvsrconnect.service.EventService;
import com.mvsr.mvsrconnect.service.PushNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;
    private final EventRepository eventRepository;
    private final EventEnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final PushNotificationService pushNotificationService;

    public EventController(EventService eventService,
                           EventRepository eventRepository,
                           EventEnrollmentRepository enrollmentRepository,
                           UserRepository userRepository,
                           PushNotificationService pushNotificationService) {
        this.eventService = eventService;
        this.eventRepository = eventRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.pushNotificationService = pushNotificationService;
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private User resolveUser(OAuth2User principal) {
        String email = principal.getAttribute("email");
        return userRepository.findByEmail(email).orElseThrow();
    }

    // ─── Public: list all active events ─────────────────────────────────────────

    @GetMapping
    public ResponseEntity<?> listEvents(@AuthenticationPrincipal OAuth2User principal) {
        List<Map<String, Object>> events = eventService.getActiveEvents()
                .stream()
                .map(eventService::toPublicView)
                .collect(Collectors.toList());
        return ResponseEntity.ok(events);
    }

    // ─── Public: single event detail ────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<?> getEvent(@PathVariable Long id,
                                      @AuthenticationPrincipal OAuth2User principal) {
        try {
            Event event = eventService.getEventById(id);
            return ResponseEntity.ok(eventService.toPublicView(event));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ─── Create event (any authenticated user can organise — or restrict to ADMIN/MOD) ──

    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody Event event,
                                         @AuthenticationPrincipal OAuth2User principal) {
        User user = resolveUser(principal);

        // Only ADMIN or MODERATOR can create events
        if (user.getRole() != Role.ADMIN && user.getRole() != Role.MODERATOR) {
            return ResponseEntity.status(403).body("Only moderators and admins can create events.");
        }

        event.setOrganizerId(user.getId());
        event.setOrganizerName(user.getName());

        try {
            Event created = eventService.createEvent(event);
            return ResponseEntity.ok(eventService.toPublicView(created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ─── Cancel event ────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelEvent(@PathVariable Long id,
                                         @AuthenticationPrincipal OAuth2User principal) {
        User user = resolveUser(principal);
        Event event = eventRepository.findById(id).orElseThrow();

        boolean isOrganizer = event.getOrganizerId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if (!isOrganizer && !isAdmin) {
            return ResponseEntity.status(403).body("Not authorized.");
        }

        eventService.cancelEvent(id);
        return ResponseEntity.ok().build();
    }

    // ─── Enroll ──────────────────────────────────────────────────────────────────

    @PostMapping("/{id}/enroll")
    public ResponseEntity<?> enroll(@PathVariable Long id,
                                    @AuthenticationPrincipal OAuth2User principal) {
        User user = resolveUser(principal);
        try {
            EventEnrollment enrollment = eventService.enroll(id, user);

            // For paid events, also return the UPI deeplink (never stored in browser)
            if (enrollment.getStatus() == EnrollmentStatus.PENDING_PAYMENT) {
                Event event = eventService.getEventById(id);
                String deeplink = eventService.buildUpiDeeplink(event, enrollment.getId());
                return ResponseEntity.ok(Map.of(
                        "enrollment", enrollment,
                        "upiDeeplink", deeplink,
                        "amountInPaise", event.getFeeInPaise()
                ));
            }

            return ResponseEntity.ok(enrollment);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ─── Student submits UTR after paying ────────────────────────────────────────

    @PostMapping("/{id}/pay")
    public ResponseEntity<?> submitPayment(@PathVariable Long id,
                                           @RequestBody Map<String, String> body,
                                           @AuthenticationPrincipal OAuth2User principal) {
        User user = resolveUser(principal);
        String utr = body.get("utrNumber");
        try {
            EventEnrollment updated = eventService.submitPayment(id, user.getId(), utr);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ─── Student: get their own ticket/enrollment for an event ──────────────────

    @GetMapping("/{id}/my-enrollment")
    public ResponseEntity<?> myEnrollment(@PathVariable Long id,
                                          @AuthenticationPrincipal OAuth2User principal) {
        User user = resolveUser(principal);
        return enrollmentRepository.findByEventIdAndUserId(id, user.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ─── Student: all their tickets ──────────────────────────────────────────────

    @GetMapping("/my-tickets")
    public ResponseEntity<?> myTickets(@AuthenticationPrincipal OAuth2User principal) {
        User user = resolveUser(principal);
        return ResponseEntity.ok(eventService.getMyTickets(user.getId()));
    }

    // ─── Manager: view enrollments for their event ───────────────────────────────

    @GetMapping("/{id}/enrollments")
    public ResponseEntity<?> getEnrollments(@PathVariable Long id,
                                            @AuthenticationPrincipal OAuth2User principal) {
        User user = resolveUser(principal);

        // Admin can see any event's enrollments
        if (user.getRole() == Role.ADMIN) {
            return ResponseEntity.ok(
                    enrollmentRepository.findByEventIdOrderByEnrolledAtAsc(id)
            );
        }

        try {
            return ResponseEntity.ok(eventService.getEnrollmentsForEvent(id, user.getId()));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    // ─── Manager: approve a payment ──────────────────────────────────────────────

    @PostMapping("/enrollments/{enrollmentId}/approve")
    public ResponseEntity<?> approveEnrollment(@PathVariable Long enrollmentId,
                                               @AuthenticationPrincipal OAuth2User principal) {
        User user = resolveUser(principal);
        try {
            EventEnrollment updated = eventService.approveEnrollment(enrollmentId, user.getId());

            pushNotificationService.notifyEventEnrollmentApproved(
                    updated.getUserId(), eventRepository.findById(updated.getEventId())
                            .map(Event::getTitle).orElse("an event"));

            return ResponseEntity.ok(updated);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ─── Manager: reject a payment ────────────────────────────────────────────────

    @PostMapping("/enrollments/{enrollmentId}/reject")
    public ResponseEntity<?> rejectEnrollment(@PathVariable Long enrollmentId,
                                              @AuthenticationPrincipal OAuth2User principal) {
        User user = resolveUser(principal);
        try {
            EventEnrollment updated = eventService.rejectEnrollment(enrollmentId, user.getId());
            return ResponseEntity.ok(updated);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ─── Scanner: verify QR (GET — called when camera reads the URL) ─────────────

    @GetMapping("/verify/{token}")
    public ResponseEntity<?> verifyQr(@PathVariable String token,
                                      @AuthenticationPrincipal OAuth2User principal) {
        try {
            return ResponseEntity.ok(eventService.verifyQr(token));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Scanner: check in (POST — manager taps confirm after seeing the details) ──

    @PostMapping("/checkin/{token}")
    public ResponseEntity<?> checkIn(@PathVariable String token,
                                     @AuthenticationPrincipal OAuth2User principal) {
        User user = resolveUser(principal);
        try {
            // Admin bypass: resolve the event's actual organizer ID so the service
            // auth check passes. Admins may scan at any event.
            Long actorId = user.getId();
            if (user.getRole() == Role.ADMIN) {
                var enrollment = enrollmentRepository.findByQrToken(token)
                        .orElseThrow(() -> new IllegalStateException("Invalid QR."));
                Event event = eventRepository.findById(enrollment.getEventId()).orElseThrow();
                actorId = event.getOrganizerId();
            }
            Map<String, Object> result = eventService.checkIn(token, actorId);
            return ResponseEntity.ok(result);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── Manager: their events dashboard ─────────────────────────────────────────

    @GetMapping("/my-events")
    public ResponseEntity<?> myEvents(@AuthenticationPrincipal OAuth2User principal) {
        User user = resolveUser(principal);
        List<Map<String, Object>> events = eventService.getMyEvents(user.getId())
                .stream()
                .map(eventService::toPublicView)
                .collect(Collectors.toList());
        return ResponseEntity.ok(events);
    }
}
