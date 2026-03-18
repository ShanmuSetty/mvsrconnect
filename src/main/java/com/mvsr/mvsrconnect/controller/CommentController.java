package com.mvsr.mvsrconnect.controller;

import com.mvsr.mvsrconnect.model.Comment;
import com.mvsr.mvsrconnect.model.Post;
import com.mvsr.mvsrconnect.model.User;
import com.mvsr.mvsrconnect.repository.CommentRepository;
import com.mvsr.mvsrconnect.repository.PostRepository;
import com.mvsr.mvsrconnect.repository.UserRepository;
import com.mvsr.mvsrconnect.service.ModerationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/posts")
public class CommentController {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ModerationService moderationService;

    public CommentController(CommentRepository commentRepository,
                             PostRepository postRepository,
                             UserRepository userRepository,
                             ModerationService moderationService){

        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.moderationService = moderationService;
    }

    @PostMapping("/{postId}/comment")
    public Comment createComment(@PathVariable Long postId,
                                 @RequestBody Comment comment,
                                 @AuthenticationPrincipal OAuth2User principal) {

        String email = principal.getAttribute("email");
        User user = userRepository.findByEmail(email).orElseThrow();

        String context = "";

        if(comment.getParentCommentId() != null){

            Comment parent = commentRepository
                    .findById(comment.getParentCommentId())
                    .orElse(null);

            if(parent != null){
                context = parent.getContent();
            }
        }

        if(comment.getContent() != null &&
                moderationService.isTextToxic(comment.getContent(), context)){
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Toxic language detected. Please keep discussions respectful."
            );
        }

        Post post = postRepository.findById(postId).orElseThrow();

        Comment newComment = new Comment();

        newComment.setContent(comment.getContent());
        newComment.setPost(post);
        newComment.setUser(user);
        newComment.setParentCommentId(comment.getParentCommentId());
        newComment.setCreatedAt(LocalDateTime.now());

        return commentRepository.save(newComment);
    }
    @GetMapping("/{postId}/comments")
    public List<Comment> getComments(@PathVariable Long postId){
        return commentRepository.findByPost_IdOrderByCreatedAtAsc(postId);
    }
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable Long id,
                                           @AuthenticationPrincipal OAuth2User principal){

        String email = principal.getAttribute("email");

        User user = userRepository.findByEmail(email).orElseThrow();

        Comment comment = commentRepository.findById(id).orElseThrow();

        if(!comment.getUser().getId().equals(user.getId())){
            return ResponseEntity.status(403).body("Not your comment");
        }

        commentRepository.deleteById(id);

        return ResponseEntity.ok().build();
    }
}