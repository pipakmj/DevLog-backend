package com.devlog.devlog.auth.service;

import com.devlog.devlog.auth.dto.dashboard.DashboardKpiResponse;
import com.devlog.devlog.auth.repository.PostRepository;
import com.devlog.devlog.auth.repository.ProjectRepository;
import com.devlog.devlog.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public DashboardKpiResponse getKpiData() {
        long totalUsers = userRepository.count();
        long totalProjects = projectRepository.count();
        long totalPosts = postRepository.count();

        // TODO: Implement ReportEntity and counting logic
        long unprocessedReports = 0;

        return DashboardKpiResponse.builder()
                .totalUsers(totalUsers)
                .totalProjects(totalProjects)
                .totalPosts(totalPosts)
                .unprocessedReports(unprocessedReports)
                .build();
    }
}
