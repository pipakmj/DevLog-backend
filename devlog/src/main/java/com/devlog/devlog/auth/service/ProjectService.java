package com.devlog.devlog.auth.service;

import com.devlog.devlog.auth.dto.ProjectRequest;
import com.devlog.devlog.auth.dto.ProjectResponse;
import com.devlog.devlog.auth.entity.ProjectEntity;
import com.devlog.devlog.auth.entity.UserEntity;
import com.devlog.devlog.auth.repository.ProjectRepository;
import com.devlog.devlog.auth.repository.UserRepository;
import com.devlog.devlog.global.exception.BusinessException;
import com.devlog.devlog.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects() {
        List<ProjectEntity> projects = projectRepository.findAll();
        return projects.stream().map(ProjectResponse::getUserProjectResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getUserProjects(String userEmail) {
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<ProjectEntity> projects = projectRepository.findByUserEntityId(user.getId());

        return projects.stream()
                .map(ProjectResponse::getUserProjectResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProjectResponse createProject(String userEmail, ProjectRequest request) {
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        ProjectEntity project = ProjectEntity.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .demoUrl(request.getDemoUrl())
                .githubUrl(request.getGithubUrl())
                .techStack(request.getTechStack())
                .thumbnail(request.getThumbnail())
                .userEntity(user)
                .createdAt(LocalDateTime.now())
                .build();

        ProjectEntity savedProject = projectRepository.save(project);
        return ProjectResponse.getUserProjectResponse(savedProject);
    }

    @Transactional
    public ProjectResponse updateProject(Long projectId, String userEmail, ProjectRequest request) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        if (!project.getUserEntity().getEmail().equals(userEmail)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_PROJECT_ACCESS);
        }

        if (request.getTitle() != null) project.setTitle(request.getTitle());
        if (request.getDescription() != null) project.setDescription(request.getDescription());
        if (request.getDemoUrl() != null) project.setDemoUrl(request.getDemoUrl());
        if (request.getGithubUrl() != null) project.setGithubUrl(request.getGithubUrl());
        if (request.getTechStack() != null) project.setTechStack(request.getTechStack());
        if (request.getThumbnail() != null) project.setThumbnail(request.getThumbnail());

        return ProjectResponse.getUserProjectResponse(project);
    }

    @Transactional
    public void deleteProject(Long projectId, String userEmail) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        if (!project.getUserEntity().getEmail().equals(userEmail)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_PROJECT_ACCESS);
        }

        projectRepository.delete(project);
    }
}
