package com.mvsr.mvsrconnect.repository;

import com.mvsr.mvsrconnect.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByActiveTrueOrderByEventDateAsc();

    List<Event> findByOrganizerIdOrderByCreatedAtDesc(Long organizerId);

    List<Event> findByClubIdAndActiveTrue(Long clubId);
}
