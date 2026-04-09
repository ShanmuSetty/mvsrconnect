// ─── FoodStallRepository.java ───────────────────────────────────────────────
package com.mvsr.mvsrconnect.repository;

import com.mvsr.mvsrconnect.model.VendorSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface VendorSessionRepository extends JpaRepository<VendorSession, Long> {
    Optional<VendorSession> findBySessionToken(String token);
    void deleteByExpiresAtBefore(LocalDateTime now);
}


