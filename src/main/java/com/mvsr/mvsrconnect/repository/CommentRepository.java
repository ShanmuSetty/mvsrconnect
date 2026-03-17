package com.mvsr.mvsrconnect.repository;

import com.mvsr.mvsrconnect.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment,Long> {


    List<Comment> findByPost_IdOrderByCreatedAtAsc(Long postId);

    void deleteByPost_Id(Long postId);

    List<Comment> findByParentCommentId(Long parentId);

    List<Comment> findByUser_IdOrderByCreatedAtDesc(Long userId);
}