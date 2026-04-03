package com.mvsr.mvsrconnect.controller;

import com.mvsr.mvsrconnect.model.User;
import com.mvsr.mvsrconnect.model.Vote;
import com.mvsr.mvsrconnect.repository.UserRepository;
import com.mvsr.mvsrconnect.repository.VoteRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/votes")
public class VoteController {

    private final VoteRepository voteRepository;
    private final UserRepository userRepository;

    public VoteController(VoteRepository voteRepository,
                          UserRepository userRepository) {
        this.voteRepository = voteRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/{postId}")
    public ResponseEntity<Map<String, Object>> vote(
            @PathVariable Long postId,
            @RequestParam int value,
            @AuthenticationPrincipal OAuth2User principal) {

        String email = principal.getAttribute("email");
        User user = userRepository.findByEmail(email).orElseThrow();

        Optional<Vote> existing = voteRepository.findByUserIdAndPostId(user.getId(), postId);

        if (existing.isPresent()) {
            Vote vote = existing.get();
            if (vote.getValue() == value) {
                voteRepository.delete(vote);
            } else {
                vote.setValue(value);
                voteRepository.save(vote);
            }
        } else {
            Vote vote = new Vote();
            vote.setPostId(postId);
            vote.setUserId(user.getId());
            vote.setValue(value);
            voteRepository.save(vote);
        }

        // Return all 3 values in one response — eliminates 2 extra round trips
        return ResponseEntity.ok(Map.of(
                "up",   voteRepository.getUpvotes(postId),
                "down", voteRepository.getDownvotes(postId),
                "user", voteRepository.findByUserIdAndPostId(user.getId(), postId)
                        .map(Vote::getValue).orElse(0)
        ));
    }

    // Keep these for initial page load (still used by loadVotes on open)
    @GetMapping("/up/{postId}")
    public int getUpvotes(@PathVariable Long postId) {
        return voteRepository.getUpvotes(postId);
    }

    @GetMapping("/down/{postId}")
    public int getDownvotes(@PathVariable Long postId) {
        return voteRepository.getDownvotes(postId);
    }

    @GetMapping("/user/{postId}")
    public int getUserVote(@PathVariable Long postId,
                           @AuthenticationPrincipal OAuth2User principal) {
        String email = principal.getAttribute("email");
        User user = userRepository.findByEmail(email).orElseThrow();
        return voteRepository
                .findByUserIdAndPostId(user.getId(), postId)
                .map(Vote::getValue)
                .orElse(0);
    }
}