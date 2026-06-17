package com.devlog.devlog.auth.service;

import com.devlog.devlog.auth.dto.portfolio.PortfolioBuilderProjectResponse;
import com.devlog.devlog.auth.entity.UserEntity;
import com.devlog.devlog.auth.repository.ProjectRepository;
import com.devlog.devlog.auth.repository.UserRepository;
import com.devlog.devlog.global.common.CustomPageResponse;
import com.devlog.devlog.global.exception.BusinessException;
import com.devlog.devlog.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PortfolioBuilderService {
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public CustomPageResponse<PortfolioBuilderProjectResponse> getPortfolioBuilderProjects(String userEmail,
            Pageable pageable) {
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return new CustomPageResponse<>(
                projectRepository.findPageByUserEntityId(user.getId(), pageable)
                        .map(PortfolioBuilderProjectResponse::toResponse));
    }
}
