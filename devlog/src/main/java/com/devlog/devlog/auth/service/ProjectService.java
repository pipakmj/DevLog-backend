package com.devlog.devlog.auth.service;

import com.devlog.devlog.auth.dto.project.ProjectRequest;
import com.devlog.devlog.auth.dto.project.ProjectResponse;
import com.devlog.devlog.auth.entity.ProjectEntity;
import com.devlog.devlog.auth.entity.UserEntity;
import com.devlog.devlog.auth.repository.ProjectRepository;
import com.devlog.devlog.auth.repository.UserRepository;
import com.devlog.devlog.global.common.CustomSliceResponse;
import com.devlog.devlog.global.exception.BusinessException;
import com.devlog.devlog.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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
    @Cacheable(value = "projectAllList", key = "#pageable.pageNumber")
    public CustomSliceResponse<ProjectResponse> getAllProjects(Pageable pageable) {
        Slice<ProjectResponse> slice = projectRepository.findAll(pageable).map(ProjectResponse::getUserProjectResponse);
        return new CustomSliceResponse<>(slice);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "projectUserList", key = "#userEmail+#pageable.pageNumber")
    public CustomSliceResponse<ProjectResponse> getUserProjects(String userEmail, Pageable pageable) {
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Slice<ProjectResponse> sice = projectRepository.findByUserEntityId(user.getId(), pageable)
                .map(ProjectResponse::getUserProjectResponse);
        return new CustomSliceResponse<>(sice);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getUserProjectsToPost(String userEmail) {
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return projectRepository.findByUserEntityId(user.getId())
                .stream()
                .map(ProjectResponse::getUserProjectResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "projectDetail", key = "#id")
    public ProjectResponse getDetailProject(Long id) {
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
        return ProjectResponse.getUserProjectResponse(project);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "projectAllList", allEntries = true),
            @CacheEvict(value = "projectUserList", allEntries = true)
    })
    public ProjectResponse createProject(String userEmail, ProjectRequest request) {
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String thumbnailUrl = request.getThumbnail();
        if (thumbnailUrl != null && !thumbnailUrl.startsWith("https://res.cloudinary.com/")) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        ProjectEntity project = ProjectEntity.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .demoUrl(request.getDemoUrl())
                .githubUrl(request.getGithubUrl())
                .techStack(request.getTechStack())
                .myRole(request.getMyRole())
                .thumbnail(request.getThumbnail())
                .userEntity(user)
                .createdAt(LocalDateTime.now())
                .build();

        ProjectEntity savedProject = projectRepository.save(project);
        return ProjectResponse.getUserProjectResponse(savedProject);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "projectAllList", allEntries = true),
            @CacheEvict(value = "projectUserList", allEntries = true),
            @CacheEvict(value = "projectDetail", key = "#projectId")
    })
    public ProjectResponse updateProject(Long projectId, String userEmail, ProjectRequest request) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        if (!project.getUserEntity().getEmail().equals(userEmail)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_PROJECT_ACCESS);
        }
        String thumbnailUrl = request.getThumbnail();
        if (thumbnailUrl != null && !thumbnailUrl.startsWith("https://res.cloudinary.com/")) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (request.getTitle() != null)
            project.setTitle(request.getTitle());
        if (request.getDescription() != null)
            project.setDescription(request.getDescription());
        if (request.getDemoUrl() != null)
            project.setDemoUrl(request.getDemoUrl());
        if (request.getGithubUrl() != null)
            project.setGithubUrl(request.getGithubUrl());
        if (request.getMyRole() != null)
            project.setMyRole(request.getMyRole());
        if (request.getTechStack() != null)
            project.setTechStack(request.getTechStack());
        if (request.getThumbnail() != null)
            project.setThumbnail(request.getThumbnail());

        return ProjectResponse.getUserProjectResponse(project);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "projectAllList", allEntries = true),
            @CacheEvict(value = "projectUserList", allEntries = true),
            @CacheEvict(value = "projectDetail", key = "#projectId")
    })
    public void deleteProject(Long projectId, String userEmail) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        if (!project.getUserEntity().getEmail().equals(userEmail)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_PROJECT_ACCESS);
        }

        projectRepository.delete(project);
    }
}
