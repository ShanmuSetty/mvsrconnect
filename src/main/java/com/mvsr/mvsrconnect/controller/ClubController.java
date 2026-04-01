package com.mvsr.mvsrconnect.controller;

import com.mvsr.mvsrconnect.model.*;
import com.mvsr.mvsrconnect.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/clubs")
public class ClubController {

    private final ClubRepository clubRepository;
    private final ClubJoinRequestRepository joinRepository;
    private final UserRepository userRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final PostRepository postRepository;
    private final JavaMailSender mailSender;

    public ClubController(
            ClubRepository clubRepository,
            ClubJoinRequestRepository joinRepository,
            UserRepository userRepository,
            ClubMemberRepository clubMemberRepository,
            PostRepository postRepository,
            JavaMailSender mailSender){

        this.clubRepository = clubRepository;
        this.joinRepository = joinRepository;
        this.userRepository = userRepository;
        this.clubMemberRepository = clubMemberRepository;
        this.postRepository = postRepository;
        this.mailSender = mailSender;
    }

    @GetMapping
    public List<Club> getClubs(){
        return clubRepository.findAll();
    }

    @PostMapping("/{clubId}/join-request")
    public ClubJoinRequest requestJoin(
            @PathVariable Long clubId,
            @AuthenticationPrincipal OAuth2User oauthUser){

        String email = oauthUser.getAttribute("email");

        User user = userRepository.findByEmail(email).orElseThrow();


        if(clubMemberRepository.findByUserIdAndClubId(user.getId(), clubId).isPresent()){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Already a member of this club"
            );
        }


        if(joinRepository
                .findByUser_IdAndClub_IdAndStatus(user.getId(), clubId, RequestStatus.PENDING)
                .isPresent()){

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Join request already pending"
            );
        }

        Club club = clubRepository.findById(clubId).orElseThrow();

        ClubJoinRequest req = new ClubJoinRequest();
        req.setUser(user);
        req.setClub(club);
        req.setStatus(RequestStatus.PENDING);
        req.setCreatedAt(LocalDateTime.now());

        return joinRepository.save(req);
    }

    @GetMapping("/{clubId}/requests")
    public List<ClubJoinRequest> getJoinRequests(@PathVariable Long clubId){
        return joinRepository.findByClubId(clubId);
    }

    @PostMapping("/requests/{requestId}/approve")
    public ClubJoinRequest approve(@PathVariable Long requestId){

        ClubJoinRequest req = joinRepository.findById(requestId).orElseThrow();

        req.setStatus(RequestStatus.APPROVED);

        ClubMember member = new ClubMember();
        member.setUser(req.getUser());
        member.setClub(req.getClub());
        member.setRole(ClubRole.MEMBER);
        member.setJoinedAt(LocalDateTime.now());

        clubMemberRepository.save(member);

        // Send approval email
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(req.getUser().getEmail());
            mail.setSubject("You've been approved to join " + req.getClub().getName() + " on MvsrConnect!");
            mail.setText("Hi " + req.getUser().getName() + ",\n\n" +
                    "Your request to join the club \"" + req.getClub().getName() + "\" has been approved.\n\n" +
                    "You can now post in the club and access club content on MvsrConnect.\n\n" +
                    "— MvsrConnect Team");
            mailSender.send(mail);
        } catch (Exception e) {
            System.out.println("Email sending failed: " + e.getMessage());
        }

        return joinRepository.save(req);
    }

    @PostMapping("/requests/{requestId}/reject")
    public ClubJoinRequest reject(@PathVariable Long requestId){

        ClubJoinRequest req = joinRepository.findById(requestId).orElseThrow();

        req.setStatus(RequestStatus.REJECTED);

        return joinRepository.save(req);
    }

    @GetMapping("/requests")
    public List<ClubJoinRequest> getRequests(){
        return joinRepository.findByStatus(RequestStatus.PENDING);
    }

    @GetMapping("/my-status")
    public java.util.Map<String, java.util.Set<Long>> myClubStatus(
            @AuthenticationPrincipal OAuth2User oauthUser) {

        if (oauthUser == null) return java.util.Map.of("member", java.util.Set.of(), "pending", java.util.Set.of());

        String email = oauthUser.getAttribute("email");
        User user = userRepository.findByEmail(email).orElseThrow();

        java.util.Set<Long> memberOf = clubMemberRepository.findByUserId(user.getId())
                .stream().map(m -> m.getClub().getId())
                .collect(java.util.stream.Collectors.toSet());

        java.util.Set<Long> pending = joinRepository
                .findByUser_IdAndClub_IdAndStatus(user.getId(), -1L, RequestStatus.PENDING)
                .map(r -> java.util.Set.of(r.getClub().getId()))
                .orElse(java.util.Set.of());

        // Use the status-based finder properly
        java.util.Set<Long> pendingSet = joinRepository.findByStatus(RequestStatus.PENDING)
                .stream()
                .filter(r -> r.getUser().getId().equals(user.getId()))
                .map(r -> r.getClub().getId())
                .collect(java.util.stream.Collectors.toSet());

        return java.util.Map.of("member", memberOf, "pending", pendingSet);
    }

    @GetMapping("/{clubId}/posts")
    public List<Post> getPostsByClub(@PathVariable Long clubId) {
        return postRepository.findByClub_IdOrderByCreatedAtDesc(clubId);
    }
}