package com.mvsr.mvsrconnect.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String googleId;

    @Column(unique = true)
    private String name;

    @Column(length = 500)
    private String bio;

    @Column(unique = true)
    private String email;

    @Column(length = 512)
    private String picture;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;
}