package com.devlog.devlog.auth.dto;

import com.devlog.devlog.auth.entity.ProjectEntity;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    private Long id;
    private String title;
    private String description;
    private String demoUrl;
    private String githubUrl;
    private String techStack;
    private String thumbnail;
    private LocalDateTime createdAt;

    public static ProjectResponse getUserProjectResponse(ProjectEntity projectEntity) {
        return ProjectResponse.builder()
                .id(projectEntity.getId())
                .title(projectEntity.getTitle())
                .description(projectEntity.getDescription())
                .demoUrl(projectEntity.getDemoUrl())
                .githubUrl(projectEntity.getGithubUrl())
                .techStack(projectEntity.getTechStack())
                .thumbnail(projectEntity.getThumbnail())
                .createdAt(projectEntity.getCreatedAt())
                .build();
    }
}
