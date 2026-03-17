package com.mvsr.mvsrconnect.repository;

import com.mvsr.mvsrconnect.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByStatus(String status);
    List<Report> findByPostId(Long postId);

}