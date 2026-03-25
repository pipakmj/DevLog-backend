package com.devlog.devlog.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "refresh_token")
@Builder
public class RefreshTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(nullable = false, unique = true)
    String token;
    @Column(nullable = false, unique = true)
    String email;
    @Column(nullable = false)
    LocalDateTime expiryTime;
    @Column(nullable = false)
    LocalDateTime createAt = LocalDateTime.now();
}
