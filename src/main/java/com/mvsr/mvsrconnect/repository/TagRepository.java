package com.mvsr.mvsrconnect.repository;

import com.mvsr.mvsrconnect.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {
    @Query("SELECT t FROM Tag t WHERE LOWER(t.name) LIKE %:term%")
    List<Tag> searchTags(@Param("term") String term);

}