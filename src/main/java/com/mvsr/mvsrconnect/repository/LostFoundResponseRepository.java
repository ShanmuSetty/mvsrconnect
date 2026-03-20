package com.mvsr.mvsrconnect.repository;

import com.mvsr.mvsrconnect.model.LostFoundResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LostFoundResponseRepository extends JpaRepository<LostFoundResponse, Long> {
    List<LostFoundResponse> findByItemIdOrderByCreatedAtAsc(Long itemId);
    long countByItemId(Long itemId);
    boolean existsByItemIdAndAuthorId(Long itemId, Long authorId);
}