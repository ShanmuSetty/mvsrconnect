package com.mvsr.mvsrconnect.repository;

import com.mvsr.mvsrconnect.model.ModQueue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModQueueRepository extends JpaRepository<ModQueue, Long> {

    List<ModQueue> findByClubId(Long clubId);

}