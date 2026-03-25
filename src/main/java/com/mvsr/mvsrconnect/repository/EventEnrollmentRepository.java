package com.mvsr.mvsrconnect.repository;

import com.mvsr.mvsrconnect.model.EnrollmentStatus;
import com.mvsr.mvsrconnect.model.EventEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventEnrollmentRepository extends JpaRepository<EventEnrollment, Long> {

    Optional<EventEnrollment> findByEventIdAndUserId(Long eventId, Long userId);

    boolean existsByEventIdAndUserId(Long eventId, Long userId);

    List<EventEnrollment> findByEventIdOrderByEnrolledAtAsc(Long eventId);

    List<EventEnrollment> findByEventIdAndStatusOrderByEnrolledAtAsc(Long eventId, EnrollmentStatus status);

    List<EventEnrollment> findByUserIdOrderByEnrolledAtDesc(Long userId);

    Optional<EventEnrollment> findByQrToken(String qrToken);

    int countByEventIdAndStatus(Long eventId, EnrollmentStatus status);
}
