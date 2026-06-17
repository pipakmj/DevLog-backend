package com.devlog.devlog.auth.service;

import com.devlog.devlog.auth.dto.portfolio.request.CreatePorfolioRequest;
import com.devlog.devlog.auth.dto.portfolio.request.FeatureDTO;
import com.devlog.devlog.auth.dto.portfolio.request.PortfolioImageDTO;
import com.devlog.devlog.auth.dto.portfolio.request.TroubleshootDTO;
import com.devlog.devlog.auth.dto.portfolio.response.PortfolioDetailResponse;
import com.devlog.devlog.auth.dto.portfolio.response.PortfolioResponse;
import com.devlog.devlog.auth.entity.PortfolioEntity;
import com.devlog.devlog.auth.entity.ProjectEntity;
import com.devlog.devlog.auth.entity.UserEntity;
import com.devlog.devlog.auth.repository.PortfolioRepository;
import com.devlog.devlog.auth.repository.ProjectRepository;
import com.devlog.devlog.auth.repository.UserRepository;
import com.devlog.devlog.global.exception.BusinessException;
import com.devlog.devlog.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioService {
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final PortfolioRepository portfolioRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public PortfolioResponse createPortfolio(
            String userEmail,
            CreatePorfolioRequest request
    ) throws JsonProcessingException {

        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        ProjectEntity project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        if(!project.getUserEntity().equals(user)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_PROJECT_ACCESS);
        }
        PortfolioEntity portfolio = portfolioRepository.save(
                PortfolioEntity.builder()
                        .overview(request.getOverview())
                        .roles(request.getRoles())
                        .techStackJson(
                                objectMapper.writeValueAsString(
                                        request.getTechStack()
                                )
                        )
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
    @Transactional(readOnly = true)
    public PortfolioDetailResponse getPortfolio(String userEmail, Long portfolioId) {
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        PortfolioEntity portfolioEntity = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PORTFOLIO_NOT_FOUND));
        if(!portfolioEntity.getProject().getUserEntity().equals(user)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_PORTFOLIO_ACCESS);
        }
        try{
        List<String> techStack =
                objectMapper.readValue(
                        portfolioEntity.getTechStackJson(),
                        new TypeReference<List<String>>() {}
                );

        List<FeatureDTO> features =
                objectMapper.readValue(
                        portfolioEntity.getFeaturesJson(),
                        new TypeReference<List<FeatureDTO>>() {}
                );

        List<TroubleshootDTO> troubleshoots =
                objectMapper.readValue(
                        portfolioEntity.getTroubleshootsJson(),
                        new TypeReference<List<TroubleshootDTO>>() {}
                );

        PortfolioImageDTO images =
                objectMapper.readValue(
                        portfolioEntity.getImagesJson(),
                        PortfolioImageDTO.class
                );
            return  PortfolioDetailResponse.builder()
                    .id(portfolioEntity.getId())
                    .projectId(portfolioEntity.getProject().getId())
                    .projectName(portfolioEntity.getProject().getTitle())
                    .overview(portfolioEntity.getOverview())
                    .roles(portfolioEntity.getRoles())
                    .techStack(techStack)
                    .features(features)
                    .troubleshoots(troubleshoots)
                    .metrics(portfolioEntity.getMetrics())
                    .images(images)
                    .status(portfolioEntity.getStatus())
                    .isPublic(false)
                    .shareToken(null)
                    .createdAt(portfolioEntity.getCreatedAt())
                    .updatedAt(portfolioEntity.getUpdatedAt())
                    .build();
    } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
    }
    }
}
