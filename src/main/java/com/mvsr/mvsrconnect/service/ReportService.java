package com.mvsr.mvsrconnect.service;

import com.mvsr.mvsrconnect.model.Report;
import com.mvsr.mvsrconnect.repository.ReportRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository){
        this.reportRepository = reportRepository;
    }

    public Report createReport(Report report){

        report.setStatus("OPEN");
        report.setCreatedAt(LocalDateTime.now());

        return reportRepository.save(report);
    }

    public List<Report> getOpenReports(){
        return reportRepository.findByStatus("OPEN");
    }

    public void resolveReport(Long id){

        Report report = reportRepository.findById(id).orElseThrow();

        report.setStatus("RESOLVED");

        reportRepository.save(report);
    }

    public void resolveReportsByPost(Long postId){

        List<Report> reports = reportRepository.findByPostId(postId);

        for(Report r : reports){
            r.setStatus("RESOLVED");
        }

        reportRepository.saveAll(reports);
    }

}