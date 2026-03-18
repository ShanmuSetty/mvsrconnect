package com.mvsr.mvsrconnect.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.mvsr.mvsrconnect.model.Post;
import com.mvsr.mvsrconnect.model.Report;
import com.mvsr.mvsrconnect.model.Role;
import com.mvsr.mvsrconnect.model.User;
import com.mvsr.mvsrconnect.repository.PostRepository;
import com.mvsr.mvsrconnect.repository.ReportRepository;
import com.mvsr.mvsrconnect.repository.UserRepository;
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
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final Cloudinary cloudinary;
    private final ModerationService moderationService;
    private final ClubService clubService;
    private final ReportService reportService;

    public PostController(PostRepository postRepository,
                          UserRepository userRepository,
                          Cloudinary cloudinary,
                          ModerationService moderationService,
                          ClubService clubService,
                          ReportService reportService,
                          ReportRepository reportRepository){

        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.cloudinary = cloudinary;
        this.moderationService = moderationService;
        this.clubService = clubService;
        this.reportService = reportService;
        this.reportRepository = reportRepository;
    }
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

    public void resolveReportsByPost(Long postId){

        List<Report> reports = reportRepository.findByPostId(postId);

        for(Report r : reports){
            r.setStatus("RESOLVED");
        }

        reportRepository.saveAll(reports);
    }
    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody Post post,
                                        @AuthenticationPrincipal OAuth2User principal) {

        String email = principal.getAttribute("email");
        User user = userRepository.findByEmail(email).orElseThrow();

        // moderation
        String combinedText = (post.getTitle() != null ? post.getTitle() : "") + " " +
                (post.getContent() != null ? post.getContent() : "");
        if(moderationService.isTextToxic(combinedText.trim(), "")){
            return ResponseEntity
                    .badRequest()
                    .body("Toxic language detected. Please keep discussions respectful.");
        }

        if(post.getMediaUrl() != null && "image".equals(post.getMediaType())){
            if(moderationService.isImageUnsafe(post.getMediaUrl())){
                deleteMediaIfExists(post);
                return ResponseEntity.badRequest().body("Unsafe image detected.");
            }
        }

        if(post.getMediaUrl() != null && "video".equals(post.getMediaType())){
            if(moderationService.isVideoUnsafe(post.getMediaUrl())){
                deleteMediaIfExists(post);
                return ResponseEntity.badRequest().body("Unsafe video detected.");
            }
        }

        // club membership check
        if(post.getClub() != null){

            Long clubId = post.getClub().getId();

            if(!clubService.isMember(user.getId(), clubId)){
                return ResponseEntity.badRequest()
                        .body("You must join the club before posting.");
            }
        }

        Post newPost = new Post();

        newPost.setTitle(post.getTitle());
        newPost.setContent(post.getContent());
        newPost.setMediaUrl(post.getMediaUrl());
        newPost.setMediaType(post.getMediaType());
        newPost.setMediaPublicId(post.getMediaPublicId());
        newPost.setAuthorId(user.getId());
        newPost.setAuthorName(user.getName());
        newPost.setCreatedAt(LocalDateTime.now());

        if(post.getClub() != null){
            newPost.setClub(post.getClub());
        }
        if(post.getTags() != null){
            newPost.setTags(post.getTags());
        }
        return ResponseEntity.ok(postRepository.save(newPost));
    }
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

    @GetMapping("/new")
    public List<Post> newestPosts(){
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping("/hot")
    public List<Post> hotPosts(){
        return postRepository.findHotPosts();
    }

    @GetMapping("/top")
    public List<Post> topPosts(){
        return postRepository.findTopPosts();
    }

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
            System.out.println("User ID: " + user.getId());
            System.out.println("User Role: " + user.getRole());
            System.out.println("Post Author ID: " + post.getAuthorId());
            System.out.println("Post Club: " + post.getClub());
            System.out.println("isAuthor: " + isAuthor);
            System.out.println("isAdmin: " + isAdmin);
            System.out.println("isClubModerator: " + isClubModerator);
            return ResponseEntity.status(403).body("Not authorized");
        }
        try{

            if(post.getMediaPublicId()!=null){

                String resourceType = "image";

                if("video".equals(post.getMediaType())){
                    resourceType = "video";
                }

                cloudinary.uploader().destroy(
                        post.getMediaPublicId(),
                        ObjectUtils.asMap(
                                "resource_type", resourceType,
                                "invalidate", true
                        )
                );
            }

        }catch(Exception e){
            System.out.println("Cloudinary delete failed: " + e.getMessage());
        }
        reportService.resolveReportsByPost(id);
        postRepository.deleteById(id);

        return ResponseEntity.ok().build();
    }
}