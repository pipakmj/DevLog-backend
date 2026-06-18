package com.devlog.devlog.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "portfolios")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PortfolioEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String overview;
    private String roles;
    @Column(columnDefinition = "TEXT")
    private String techStackJson;
    @Column(columnDefinition = "TEXT")
    private String featuresJson;
    @Column(columnDefinition = "TEXT")
    private String troubleshootsJson;
    private String metrics;
    @Column(columnDefinition = "TEXT")
    private String imagesJson;
    private String status;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    private boolean isPublic;
    @Column(unique = true)
    private String shareToken;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projectId")
    private ProjectEntity project;
}
