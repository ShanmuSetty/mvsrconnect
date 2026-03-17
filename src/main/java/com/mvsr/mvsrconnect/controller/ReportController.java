package com.mvsr.mvsrconnect.controller;

import com.mvsr.mvsrconnect.model.Report;
import com.mvsr.mvsrconnect.model.User;
import com.mvsr.mvsrconnect.repository.UserRepository;
import com.mvsr.mvsrconnect.service.ReportService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;
    private final UserRepository userRepository;

    public ReportController(ReportService reportService,
                            UserRepository userRepository){
        this.reportService = reportService;
        this.userRepository = userRepository;
    }

    /* ---------------- CREATE REPORT ---------------- */

    @PostMapping
    public Report createReport(@RequestBody Report report,
                               @AuthenticationPrincipal OAuth2User principal){

        String email = principal.getAttribute("email");

        User user = userRepository.findByEmail(email).orElseThrow();

        report.setUserId(user.getId());

        return reportService.createReport(report);
    }

    /* ---------------- ADMIN: GET REPORTS ---------------- */

    @GetMapping
    public List<Report> getReports(){

        return reportService.getOpenReports();
    }

    /* ---------------- ADMIN: RESOLVE REPORT ---------------- */

    @PostMapping("/{id}/resolve")
    public void resolve(@PathVariable Long id){

        reportService.resolveReport(id);

    }

}