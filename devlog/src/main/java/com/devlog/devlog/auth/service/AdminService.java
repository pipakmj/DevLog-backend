package com.devlog.devlog.auth.service;

import com.devlog.devlog.auth.dto.admin.AdminUserResponse;
import com.devlog.devlog.auth.dto.post.PostResponse;
import com.devlog.devlog.auth.dto.project.ProjectResponse;
import com.devlog.devlog.auth.repository.PostRepository;
import com.devlog.devlog.auth.repository.ProjectRepository;
import com.devlog.devlog.auth.repository.UserRepository;
import com.devlog.devlog.global.common.CustomPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public CustomPageResponse<AdminUserResponse> getUsers(Pageable pageable) {
        return new CustomPageResponse<>(userRepository.findAll(pageable).map(AdminUserResponse::from));
    }

    @Transactional(readOnly = true)
    public CustomPageResponse<ProjectResponse> getProjects(Pageable pageable) {
        return new CustomPageResponse<>(projectRepository.findAll(pageable).map(ProjectResponse::getUserProjectResponse));
    }

    @Transactional(readOnly = true)
    public CustomPageResponse<PostResponse> getPosts(Pageable pageable) {
        return new CustomPageResponse<>(postRepository.findAll(pageable).map(PostResponse::from));
    }
}
