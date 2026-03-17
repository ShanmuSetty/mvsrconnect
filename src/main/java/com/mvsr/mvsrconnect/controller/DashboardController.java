package com.mvsr.mvsrconnect.controller;

import com.mvsr.mvsrconnect.model.*;
import com.mvsr.mvsrconnect.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final VoteRepository voteRepository;
    private final ClubMemberRepository clubMemberRepository;

    public DashboardController(
            UserRepository userRepository,
            PostRepository postRepository,
            CommentRepository commentRepository,
            VoteRepository voteRepository,
            ClubMemberRepository clubMemberRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.voteRepository = voteRepository;
        this.clubMemberRepository = clubMemberRepository;
    }

    // ── GET FULL DASHBOARD DATA ──
    @GetMapping
    public ResponseEntity<?> getDashboard(@AuthenticationPrincipal OAuth2User oauthUser) {
        if (oauthUser == null) return ResponseEntity.status(401).build();

        String email = oauthUser.getAttribute("email");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // My Posts
        List<Post> myPosts = postRepository.findByAuthorIdOrderByCreatedAtDesc(user.getId());

        // My Comments
        List<Comment> myComments = commentRepository.findByUser_IdOrderByCreatedAtDesc(user.getId());

        // My Club Memberships
        List<ClubMember> myMemberships = clubMemberRepository.findByUserId(user.getId());

        // My Upvoted Post IDs
        List<Long> likedPostIds = voteRepository.findByUserIdAndValue(user.getId(), 1)
                .stream()
                .map(Vote::getPostId)
                .collect(Collectors.toList());

        // Fetch the actual upvoted posts
        List<Post> likedPosts = postRepository.findAllById(likedPostIds);

        // Stats
        int totalUpvotesReceived = myPosts.stream()
                .mapToInt(p -> voteRepository.getUpvotes(p.getId()))
                .sum();

        return ResponseEntity.ok(Map.of(
                "user", user,
                "myPosts", myPosts,
                "myComments", myComments,
                "myMemberships", myMemberships,
                "likedPosts", likedPosts,
                "stats", Map.of(
                        "postCount", myPosts.size(),
                        "commentCount", myComments.size(),
                        "clubCount", myMemberships.size(),
                        "upvotesReceived", totalUpvotesReceived
                )
        ));
    }

    // ── UPDATE PROFILE (name, bio, avatar URL) ──
    @Transactional
    @PatchMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal OAuth2User oauthUser) {

        if (oauthUser == null) return ResponseEntity.status(401).build();

        String email = oauthUser.getAttribute("email");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // Validate name uniqueness before making any changes
        String newName = null;
        if (body.containsKey("name") && body.get("name") != null && !body.get("name").isBlank()) {
            newName = body.get("name").trim();
            if (!newName.equalsIgnoreCase(user.getName())) {
                if (userRepository.existsByNameIgnoreCase(newName)) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("Username already taken. Please choose a different name.");
                }
            }
            user.setName(newName);
        }

        if (body.containsKey("bio")) {
            user.setBio(body.get("bio"));
        }

        if (body.containsKey("picture") && body.get("picture") != null && !body.get("picture").isBlank()) {
            user.setPicture(body.get("picture").trim());
        }

        // Save user first
        User saved = userRepository.save(user);

        // Backfill all old posts with the new name AFTER user is saved
        if (newName != null) {
            postRepository.updateAuthorName(saved.getId(), saved.getName());
        }

        return ResponseEntity.ok(saved);
    }
}