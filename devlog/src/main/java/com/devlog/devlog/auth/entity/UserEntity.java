package com.devlog.devlog.auth.entity;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    @Column(unique = true, nullable = false)
    String email;
    String password;
    String nickname;
    String bio;
    String github_url;
    LocalDateTime created_at;
    LocalDateTime updated_at;
}
