package com.mvsr.mvsrconnect.controller;

import com.mvsr.mvsrconnect.model.*;
import com.mvsr.mvsrconnect.repository.*;
import org.springframework.http.HttpStatus;
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
    public ClubController(
            ClubRepository clubRepository,
            ClubJoinRequestRepository joinRepository,
            UserRepository userRepository,
            ClubMemberRepository clubMemberRepository,
            PostRepository postRepository){

        this.clubRepository = clubRepository;
        this.joinRepository = joinRepository;
        this.userRepository = userRepository;
        this.clubMemberRepository = clubMemberRepository;
        this.postRepository = postRepository;
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

        clubMemberRepository.save(member);

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

    @GetMapping("/{clubId}/posts")
    public List<Post> getPostsByClub(@PathVariable Long clubId) {
        return postRepository.findByClub_IdOrderByCreatedAtDesc(clubId);
    }
}