package com.mvsr.mvsrconnect.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "posts")
@Getter
@Setter
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String authorName;
    private String title;

    @Column(length = 5000)
    private String content;

    private Long authorId;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("post")
    private List<Comment> comments;

    @Column(length = 1000)
    private String mediaUrl;

    private String mediaType;

    private String mediaPublicId;

    @ManyToOne
    @JoinColumn(name="club_id")
    private Club club;

    @ManyToMany
    @JoinTable(
            name="post_tags",
            joinColumns=@JoinColumn(name="post_id"),
            inverseJoinColumns=@JoinColumn(name="tag_id")
    )
    private List<Tag> tags;

}
