package com.mvsr.mvsrconnect.repository;

import com.mvsr.mvsrconnect.model.LostFoundItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LostFoundItemRepository extends JpaRepository<LostFoundItem, Long> {

    List<LostFoundItem> findAllByOrderByCreatedAtDesc();

    List<LostFoundItem> findByAuthorIdOrderByCreatedAtDesc(Long authorId);

    long countByResolvedFalse();
    long countByResolvedTrue();

    @Query("""
        SELECT i FROM LostFoundItem i
        WHERE (
            LOWER(i.title)       LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(i.description) LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(i.location)    LIKE LOWER(CONCAT('%', :q, '%')) OR
            LOWER(i.category)    LIKE LOWER(CONCAT('%', :q, '%'))
        )
        ORDER BY i.createdAt DESC
    """)
    List<LostFoundItem> search(@Param("q") String q);
}