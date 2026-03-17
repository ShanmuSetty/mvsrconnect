package com.mvsr.mvsrconnect.repository;

import com.mvsr.mvsrconnect.model.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClubRepository extends JpaRepository<Club, Long> {
    @Query("SELECT c FROM Club c WHERE LOWER(c.name) LIKE %:term% OR LOWER(c.description) LIKE %:term%")
    List<Club> searchClubs(@Param("term") String term);
}