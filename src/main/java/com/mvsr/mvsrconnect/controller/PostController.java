package com.mvsr.mvsrconnect.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.mvsr.mvsrconnect.model.*;
import com.mvsr.mvsrconnect.repository.*;
import com.mvsr.mvsrconnect.service.ClubService;
import com.mvsr.mvsrconnect.service.ModerationService;
import com.mvsr.mvsrconnect.service.ReportService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final TagRepository tagRepository;
    private final ReportRepository reportRepository;
    private final Cloudinary cloudinary;
    private final ModerationService moderationService;
    private final ClubService clubService;
    private final ReportService reportService;

    public PostController(PostRepository postRepository,
                          UserRepository userRepository,
                          ClubRepository clubRepository,
                          TagRepository tagRepository,
                          Cloudinary cloudinary,
                          ModerationService moderationService,
                          ClubService clubService,
                          ReportService reportService,
                          ReportRepository reportRepository){

        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.clubRepository = clubRepository;
        this.tagRepository = tagRepository;
        this.cloudinary = cloudinary;
        this.moderationService = moderationService;
        this.clubService = clubService;
        this.reportService = reportService;
        this.reportRepository = reportRepository;
    }

    // ---------------- DELETE MEDIA ----------------
    private void deleteMediaIfExists(Post post){
        try{
            if(post.getMediaPublicId() != null){
                String resourceType = "image";
                if("video".equals(post.getMediaType())){
                    resourceType = "video";
                }

                cloudinary.uploader().destroy(
                        post.getMediaPublicId(),
                        ObjectUtils.asMap("resource_type", resourceType)
                );
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // ---------------- CREATE POST ----------------
    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody Post post,
                                        @AuthenticationPrincipal OAuth2User principal) {

        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        String email = principal.getAttribute("email");
        User user = userRepository.findByEmail(email).orElseThrow();

        // 🔒 TEXT MODERATION
        if (
                (post.getTitle() != null && moderationService.isTextToxic(post.getTitle(), "")) ||
                        (post.getContent() != null && moderationService.isTextToxic(post.getContent(), ""))
        ) {
            return ResponseEntity.badRequest()
                    .body("Toxic language detected.");
        }

        // 🔒 IMAGE MODERATION
        if(post.getMediaUrl() != null && "image".equals(post.getMediaType())){
            if(moderationService.isImageUnsafe(post.getMediaUrl())){
                deleteMediaIfExists(post);
                return ResponseEntity.badRequest().body("Unsafe image.");
            }
        }

        // 🔒 VIDEO MODERATION
        if(post.getMediaUrl() != null && "video".equals(post.getMediaType())){
            if(moderationService.isVideoUnsafe(post.getMediaUrl())){
                deleteMediaIfExists(post);
                return ResponseEntity.badRequest().body("Unsafe video.");
            }
        }

        // 🔒 CLUB VALIDATION
        Club club = null;
        if(post.getClub() != null){
            Long clubId = post.getClub().getId();

            club = clubRepository.findById(clubId)
                    .orElseThrow(() -> new RuntimeException("Club not found"));

            if(!clubService.isMember(user.getId(), clubId)){
                return ResponseEntity.badRequest()
                        .body("Join the club before posting.");
            }
        }

        // 🔒 TAG VALIDATION
        List<Tag> tags = null;
        if(post.getTags() != null && !post.getTags().isEmpty()){
            tags = post.getTags().stream()
                    .map(t -> tagRepository.findById(t.getId())
                            .orElseThrow(() -> new RuntimeException("Tag not found")))
                    .toList();
        }

        // ✅ CREATE CLEAN ENTITY
        Post newPost = new Post();
        newPost.setTitle(post.getTitle());
        newPost.setContent(post.getContent());
        newPost.setMediaUrl(post.getMediaUrl());
        newPost.setMediaType(post.getMediaType());
        newPost.setMediaPublicId(post.getMediaPublicId());
        newPost.setAuthorId(user.getId());
        newPost.setAuthorName(user.getName());
        newPost.setCreatedAt(LocalDateTime.now());
        newPost.setClub(club);
        newPost.setTags(tags);

        return ResponseEntity.ok(postRepository.save(newPost));
    }

    // ---------------- GET POSTS ----------------
    @GetMapping
    public List<Post> getAllPosts(){
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPost(@PathVariable Long id){
        return postRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/hot")
    public List<Post> hotPosts(){
        return postRepository.findHotPosts();
    }

    @GetMapping("/top")
    public List<Post> topPosts(){
        return postRepository.findTopPosts();
    }

    // ---------------- DELETE POST ----------------
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id,
                                        @AuthenticationPrincipal OAuth2User principal){

        String email = principal.getAttribute("email");
        User user = userRepository.findByEmail(email).orElseThrow();

        Post post = postRepository.findById(id).orElseThrow();

        boolean isAuthor = post.getAuthorId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isClubModerator = false;

        if(post.getClub() != null){
            isClubModerator = clubService.canModerate(user, post.getClub().getId());
        }

        if(!isAuthor && !isAdmin && !isClubModerator){
            return ResponseEntity.status(403).body("Not authorized");
        }

        deleteMediaIfExists(post);
        reportService.resolveReportsByPost(id);
        postRepository.deleteById(id);

        return ResponseEntity.ok().build();
    }
}