package com.mvsr.mvsrconnect.controller;

import com.mvsr.mvsrconnect.model.*;
import com.mvsr.mvsrconnect.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final TagRepository tagRepository;

    public SearchController(
            PostRepository postRepository,
            UserRepository userRepository,
            ClubRepository clubRepository,
            TagRepository tagRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.clubRepository = clubRepository;
        this.tagRepository = tagRepository;
    }

    // ── GLOBAL SEARCH — returns all 4 sections ──
    @GetMapping
    public ResponseEntity<?> search(@RequestParam String q) {
        if (q == null || q.isBlank()) {
            return ResponseEntity.badRequest().body("Query cannot be empty");
        }
        String term = q.trim().toLowerCase();

        List<Post> posts = postRepository.searchPosts(term);
        List<User> users = userRepository.searchUsers(term);
        List<Club> clubs = clubRepository.searchClubs(term);
        List<Tag>  tags  = tagRepository.searchTags(term);

        return ResponseEntity.ok(Map.of(
                "posts", posts,
                "users", users,
                "clubs", clubs,
                "tags",  tags,
                "query", q.trim()
        ));
    }

    // ── FILTER POSTS BY TAG ID ──
    @GetMapping("/tag/{tagId}")
    public ResponseEntity<?> filterByTag(@PathVariable Long tagId) {
        List<Post> posts = postRepository.findByTagId(tagId);
        return ResponseEntity.ok(posts);
    }
}