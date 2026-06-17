package com.devlog.devlog.auth.service;

import com.devlog.devlog.auth.dto.portfolio.CreatePorfolioRequest;
import com.devlog.devlog.auth.dto.portfolio.PortfolioResponse;
import com.devlog.devlog.auth.entity.PortfolioEntity;
import com.devlog.devlog.auth.entity.ProjectEntity;
import com.devlog.devlog.auth.entity.UserEntity;
import com.devlog.devlog.auth.repository.PortfolioRepository;
import com.devlog.devlog.auth.repository.ProjectRepository;
import com.devlog.devlog.auth.repository.UserRepository;
import com.devlog.devlog.global.exception.BusinessException;
import com.devlog.devlog.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PortfolioService {
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final PortfolioRepository portfolioRepository;

    @Transactional
    public PortfolioResponse createPortfolio(
            String userEmail,
            CreatePorfolioRequest request
    ) throws JsonProcessingException {

        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        ProjectEntity project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        ObjectMapper objectMapper = new ObjectMapper();

        PortfolioEntity portfolio = portfolioRepository.save(
                PortfolioEntity.builder()
                        .overview(request.getOverview())
                        .roles(request.getRoles())
                        .featuresJson(
                                objectMapper.writeValueAsString(
                                        request.getFeatures()
                                )
                        )
                        .troubleshootsJson(
                                objectMapper.writeValueAsString(
                                        request.getTroubleshoots()
                                )
                        )
                        .imagesJson(
                                objectMapper.writeValueAsString(
                                        request.getImages()
                                )
                        )
                        .metrics(request.getMetrics())
                        .status(request.getStatus())
                        .project(project)
                        .build()
        );

        return PortfolioResponse.builder()
                .portfolioId(portfolio.getId())
                .status(portfolio.getStatus())
                .updatedAt(portfolio.getUpdatedAt())
                .build();
    }

}
