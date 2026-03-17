package com.mvsr.mvsrconnect.repository;

import com.mvsr.mvsrconnect.model.Post;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByOrderByCreatedAtDesc();
    @Query(value = """
SELECT p.*
FROM posts p
LEFT JOIN votes v ON p.id = v.post_id
GROUP BY p.id
ORDER BY
COALESCE(SUM(v.value),0) /
POWER(EXTRACT(EPOCH FROM (NOW() - p.created_at))/3600 + 2, 1.5)
DESC
""", nativeQuery = true)
    List<Post> findHotPosts();

    @Query(value = """
SELECT p.*
FROM posts p
LEFT JOIN votes v ON p.id = v.post_id
GROUP BY p.id
ORDER BY COALESCE(SUM(v.value),0) DESC
""", nativeQuery = true)
    List<Post> findTopPosts();

    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.authorName = :newName WHERE p.authorId = :userId")
    void updateAuthorName(@Param("userId") Long userId, @Param("newName") String newName);

    // Full-text search across title, content, authorName
    @Query("SELECT p FROM Post p WHERE " +
            "LOWER(p.title) LIKE %:term% OR " +
            "LOWER(p.content) LIKE %:term% OR " +
            "LOWER(p.authorName) LIKE %:term% " +
            "ORDER BY p.createdAt DESC")
    List<Post> searchPosts(@Param("term") String term);

    // Filter by tag
    @Query("SELECT p FROM Post p JOIN p.tags t WHERE t.id = :tagId ORDER BY p.createdAt DESC")
    List<Post> findByTagId(@Param("tagId") Long tagId);
    List<Post> findByClub_IdOrderByCreatedAtDesc(Long clubId);

    List<Post> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
}
