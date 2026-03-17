package com.mvsr.mvsrconnect.repository;

import com.mvsr.mvsrconnect.model.ModeratorAppeal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ModeratorAppealRepository extends JpaRepository<ModeratorAppeal, Long> {

    List<ModeratorAppeal> findByStatus(String status);

    Optional<ModeratorAppeal> findByUserIdAndClubIdAndStatus(Long userId, Long clubId, String status);
}