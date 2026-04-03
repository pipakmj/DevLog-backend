package com.devlog.devlog.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String title;
    String description;
    String demoUrl;
    String githubUrl;
    String techStack;
    LocalDateTime createdAt;
    String thumbnail;

    @ManyToOne
    @JoinColumn(name = "userId", referencedColumnName = "id")
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    UserEntity userEntity;
}
