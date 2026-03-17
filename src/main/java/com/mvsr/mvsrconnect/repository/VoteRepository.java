package com.mvsr.mvsrconnect.repository;

import com.mvsr.mvsrconnect.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    Optional<Vote> findByUserIdAndPostId(Long userId, Long postId);

    List<Vote> findByUserIdAndValue(Long userId, int value);

    int countByPostIdAndValue(Long postId, int value);


    @Query("SELECT COUNT(v) FROM Vote v WHERE v.postId = :postId AND v.value = 1")
    int getUpvotes(Long postId);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.postId = :postId AND v.value = -1")
    int getDownvotes(Long postId);
}