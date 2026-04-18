package com.devlog.devlog.auth.dto.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRequest {
    private String title;
    private String description;
    private String demoUrl;
    private String githubUrl;
    private String techStack;
    private String thumbnail;
}
