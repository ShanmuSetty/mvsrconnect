package com.mvsr.mvsrconnect.controller;

import com.mvsr.mvsrconnect.model.*;
import com.mvsr.mvsrconnect.repository.*;
import com.mvsr.mvsrconnect.service.ClubService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mod")
public class ModController {

    private final ModQueueRepository modQueueRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final ClubRepository clubRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ClubJoinRequestRepository joinRequestRepository;
    private final ModeratorAppealRepository appealRepository;
    private final ClubService clubService;

    public ModController(
            ModQueueRepository modQueueRepository,
            ClubMemberRepository clubMemberRepository,
            ClubRepository clubRepository,
            UserRepository userRepository,
            PostRepository postRepository,
            CommentRepository commentRepository,
            ClubJoinRequestRepository joinRequestRepository,
            ModeratorAppealRepository appealRepository,
            ClubService clubService) {
        this.modQueueRepository = modQueueRepository;
        this.clubMemberRepository = clubMemberRepository;
        this.clubRepository = clubRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.joinRequestRepository = joinRequestRepository;
        this.appealRepository = appealRepository;
        this.clubService = clubService;
    }

    // ── GET MOD QUEUE FOR A CLUB ──
    @GetMapping("/queue/{clubId}")
    public List<ModQueue> getQueue(@PathVariable Long clubId) {
        return modQueueRepository.findByClubId(clubId);
    }

    // ── GET CLUBS WHERE CURRENT USER IS MODERATOR/PRESIDENT ──
    @GetMapping("/my-clubs")
    public ResponseEntity<?> getMyModClubs(@AuthenticationPrincipal OAuth2User oauthUser) {
        String email = oauthUser.getAttribute("email");
        User user = userRepository.findByEmail(email).orElseThrow();
        List<ClubMember> memberships = clubMemberRepository.findByUserId(user.getId());
        List<Club> modClubs = memberships.stream()
                .filter(m -> m.getRole() == ClubRole.MODERATOR || m.getRole() == ClubRole.PRESIDENT)
                .map(ClubMember::getClub)
                .toList();
        return ResponseEntity.ok(modClubs);
    }

    // ── GET CLUB INFO (name, members, posts) FOR MOD DASHBOARD ──
    @GetMapping("/club/{clubId}")
    public ResponseEntity<?> getClubInfo(
            @PathVariable Long clubId,
            @AuthenticationPrincipal OAuth2User oauthUser) {
        String email = oauthUser.getAttribute("email");
        User user = userRepository.findByEmail(email).orElseThrow();
        if (!clubService.canModerate(user, clubId)) {
            return ResponseEntity.status(403).body("Not authorized");
        }
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        List<ClubMember> members = clubMemberRepository.findByClubId(clubId);
        List<Post> posts = postRepository.findByClub_IdOrderByCreatedAtDesc(clubId);
        List<ClubJoinRequest> pendingRequests = joinRequestRepository
                .findByClub_IdAndStatus(clubId, RequestStatus.PENDING);
        return ResponseEntity.ok(Map.of(
                "club", club,
                "members", members,
                "posts", posts,
                "pendingRequests", pendingRequests
        ));
    }

    // ── DELETE POST (mod can delete posts in their club) ──
    @DeleteMapping("/club/{clubId}/posts/{postId}")
    public ResponseEntity<?> deletePost(
            @PathVariable Long clubId,
            @PathVariable Long postId,
            @AuthenticationPrincipal OAuth2User oauthUser) {
        String email = oauthUser.getAttribute("email");
        User user = userRepository.findByEmail(email).orElseThrow();
        if (!clubService.canModerate(user, clubId)) {
            return ResponseEntity.status(403).body("Not authorized");
        }
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (post.getClub() == null || !post.getClub().getId().equals(clubId)) {
            return ResponseEntity.status(400).body("Post does not belong to this club");
        }
        postRepository.deleteById(postId);
        return ResponseEntity.ok().build();
    }

    // ── DELETE COMMENT (mod can delete comments on posts in their club) ──
    @DeleteMapping("/club/{clubId}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long clubId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal OAuth2User oauthUser) {
        String email = oauthUser.getAttribute("email");
        User user = userRepository.findByEmail(email).orElseThrow();
        if (!clubService.canModerate(user, clubId)) {
            return ResponseEntity.status(403).body("Not authorized");
        }
        commentRepository.deleteById(commentId);
        return ResponseEntity.ok().build();
    }

    // ── APPROVE CLUB JOIN REQUEST (mod for that club) ──
    @PostMapping("/club/{clubId}/requests/{requestId}/approve")
    public ResponseEntity<?> approveRequest(
            @PathVariable Long clubId,
            @PathVariable Long requestId,
            @AuthenticationPrincipal OAuth2User oauthUser) {
        String email = oauthUser.getAttribute("email");
        User user = userRepository.findByEmail(email).orElseThrow();
        if (!clubService.canModerate(user, clubId)) {
            return ResponseEntity.status(403).body("Not authorized");
        }
        ClubJoinRequest req = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        req.setStatus(RequestStatus.APPROVED);
        ClubMember member = new ClubMember();
        member.setUser(req.getUser());
        member.setClub(req.getClub());
        member.setRole(ClubRole.MEMBER);
        member.setJoinedAt(LocalDateTime.now());
        clubMemberRepository.save(member);
        return ResponseEntity.ok(joinRequestRepository.save(req));
    }

    // ── REJECT CLUB JOIN REQUEST (mod for that club) ──
    @PostMapping("/club/{clubId}/requests/{requestId}/reject")
    public ResponseEntity<?> rejectRequest(
            @PathVariable Long clubId,
            @PathVariable Long requestId,
            @AuthenticationPrincipal OAuth2User oauthUser) {
        String email = oauthUser.getAttribute("email");
        User user = userRepository.findByEmail(email).orElseThrow();
        if (!clubService.canModerate(user, clubId)) {
            return ResponseEntity.status(403).body("Not authorized");
        }
        ClubJoinRequest req = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        req.setStatus(RequestStatus.REJECTED);
        return ResponseEntity.ok(joinRequestRepository.save(req));
    }

    // ── REMOVE A MEMBER FROM CLUB ──
    @DeleteMapping("/club/{clubId}/members/{memberId}")
    public ResponseEntity<?> removeMember(
            @PathVariable Long clubId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal OAuth2User oauthUser) {
        String email = oauthUser.getAttribute("email");
        User user = userRepository.findByEmail(email).orElseThrow();
        if (!clubService.canModerate(user, clubId)) {
            return ResponseEntity.status(403).body("Not authorized");
        }
        clubMemberRepository.deleteById(memberId);
        return ResponseEntity.ok().build();
    }

    // ── SUBMIT MODERATOR APPEAL ──
    @PostMapping("/appeal")
    public ResponseEntity<?> submitAppeal(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal OAuth2User oauthUser) {
        String email = oauthUser.getAttribute("email");
        User user = userRepository.findByEmail(email).orElseThrow();
        Long clubId = Long.valueOf(body.get("clubId").toString());
        String reason = body.get("reason").toString();
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        // Check already a moderator
        clubMemberRepository.findByUserIdAndClubId(user.getId(), clubId).ifPresent(m -> {
            if (m.getRole() == ClubRole.MODERATOR || m.getRole() == ClubRole.PRESIDENT) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Already a moderator");
            }
        });
        // Check no pending appeal
        if (appealRepository.findByUserIdAndClubIdAndStatus(user.getId(), clubId, "PENDING").isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appeal already pending");
        }
        ModeratorAppeal appeal = new ModeratorAppeal();
        appeal.setUser(user);
        appeal.setClub(club);
        appeal.setReason(reason);
        appeal.setStatus("PENDING");
        appeal.setCreatedAt(LocalDateTime.now());
        return ResponseEntity.ok(appealRepository.save(appeal));
    }

    // ── GET ALL MODERATOR APPEALS (admin only) ──
    @GetMapping("/appeals")
    public List<ModeratorAppeal> getAppeals() {
        return appealRepository.findByStatus("PENDING");
    }

    // ── APPROVE MODERATOR APPEAL (admin only → promotes user to moderator) ──
    @PostMapping("/appeals/{appealId}/approve")
    public ResponseEntity<?> approveAppeal(@PathVariable Long appealId) {
        ModeratorAppeal appeal = appealRepository.findById(appealId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        appeal.setStatus("APPROVED");
        appealRepository.save(appeal);
        // Upsert club membership with MODERATOR role
        ClubMember member = clubMemberRepository
                .findByUserIdAndClubId(appeal.getUser().getId(), appeal.getClub().getId())
                .orElse(new ClubMember());
        member.setUser(appeal.getUser());
        member.setClub(appeal.getClub());
        member.setRole(ClubRole.MODERATOR);
        if (member.getJoinedAt() == null) member.setJoinedAt(LocalDateTime.now());
        clubMemberRepository.save(member);
        return ResponseEntity.ok(appeal);
    }

    // ── REJECT MODERATOR APPEAL (admin only) ──
    @PostMapping("/appeals/{appealId}/reject")
    public ResponseEntity<?> rejectAppeal(@PathVariable Long appealId) {
        ModeratorAppeal appeal = appealRepository.findById(appealId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        appeal.setStatus("REJECTED");
        return ResponseEntity.ok(appealRepository.save(appeal));
    }

    // ── PROMOTE EXISTING MEMBER TO MODERATOR (admin assigns directly) ──
    @PostMapping("/club/{clubId}/members/{memberId}/promote")
    public ResponseEntity<?> promoteMember(
            @PathVariable Long clubId,
            @PathVariable Long memberId) {
        ClubMember member = clubMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        member.setRole(ClubRole.MODERATOR);
        return ResponseEntity.ok(clubMemberRepository.save(member));
    }
}