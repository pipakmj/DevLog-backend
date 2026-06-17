package com.devlog.devlog.auth.dto.portfolio;

import com.devlog.devlog.auth.entity.ProjectEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioBuilderProjectResponse {
    private Long projectId;
    private String projectName;
    private String thumbnailUrl;
    private String githubUrl;
    private String demoUrl;
    private List<String> techStack;
    private boolean hasPortfolio;
    private Long portfolioId;

    public static PortfolioBuilderProjectResponse toResponse(ProjectEntity project) {
        List<String> techStackList = Arrays.stream(project.getTechStack().split(","))
                .map(String::trim)
                .toList();
        return PortfolioBuilderProjectResponse.builder()
                .projectId(project.getId())
                .projectName(project.getTitle())
                .thumbnailUrl(project.getThumbnail())
                .githubUrl(project.getGithubUrl())
                .demoUrl(project.getDemoUrl())
                .techStack(techStackList)
                .hasPortfolio(project.isHasPortfolio())
                .portfolioId(project.getPortfolioEntity() != null ? project.getPortfolioEntity().getId() : null)
                .build();
    }
}
