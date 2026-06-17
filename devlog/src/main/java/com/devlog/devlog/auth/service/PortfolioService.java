package com.devlog.devlog.auth.service;

import com.devlog.devlog.auth.dto.portfolio.request.*;
import com.devlog.devlog.auth.dto.portfolio.response.PortfolioDetailResponse;
import com.devlog.devlog.auth.dto.portfolio.response.PortfolioResponse;
import com.devlog.devlog.auth.dto.portfolio.response.ProjectPortfolioResponse;
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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
            CreatePortfolioRequest request
    ) throws JsonProcessingException {

        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        ProjectEntity project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        if(!project.getUserEntity().equals(user)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_PROJECT_ACCESS);
        }
        if (Objects.equals(request.getStatus(), "COMPLETED")) {
            validateCompleted(request);
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
        return convertToResponse(portfolioEntity);
    }
    @Transactional(readOnly = true)
    public ProjectPortfolioResponse getProjectPortfolio(String userEmail, Long projectId) {
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
        if(!project.getUserEntity().equals(user)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_PROJECT_ACCESS);
        }
        Optional<PortfolioEntity> portfolioEntityOptional = portfolioRepository.findByProjectId(projectId);
        if(portfolioEntityOptional.isPresent()) {
            return ProjectPortfolioResponse.builder()
                    .exists(true)
                    .portfolio(
                          convertToResponse(portfolioEntityOptional.get())
                    )
                    .build();
        }
        return ProjectPortfolioResponse.builder()
                .exists(false)
                .portfolio(createInitialPortfolio(project))
                .build();
    }
    @Transactional
    public PortfolioResponse updatePortfolio(
            String userEmail,
            Long portfolioId,
            CreatePortfolioRequest request) throws JsonProcessingException {
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        PortfolioEntity portfolioEntity = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PORTFOLIO_NOT_FOUND));
        if(!portfolioEntity.getProject().getUserEntity().equals(user)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_PORTFOLIO_ACCESS);
        }
        if("COMPLETED".equals(request.getStatus())) {
            validateCompleted(request);
        }
        portfolioEntity.setOverview(
                request.getOverview()
        );
        portfolioEntity.setRoles(
                request.getRoles()
        );
        portfolioEntity.setMetrics(
                request.getMetrics()
        );
        portfolioEntity.setStatus(
                request.getStatus()
        );
        portfolioEntity.setTechStackJson(
                objectMapper.writeValueAsString(
                        request.getTechStack()
                )
        );
        portfolioEntity.setFeaturesJson(
                objectMapper.writeValueAsString(
                        request.getFeatures()
                )
        );
        portfolioEntity.setTroubleshootsJson(
                objectMapper.writeValueAsString(
                        request.getTroubleshoots()
                )
        );
        portfolioEntity.setImagesJson(
                objectMapper.writeValueAsString(
                        request.getImages()
                )
        );

        portfolioRepository.save(portfolioEntity);
        return PortfolioResponse.builder()
                .portfolioId(portfolioEntity.getId())
                .status(portfolioEntity.getStatus())
                .updatedAt(portfolioEntity.getUpdatedAt())
                .build();
    }

    private void validateCompleted(
            CreatePortfolioRequest request
    ) {
        if (request.getOverview() == null ||
                request.getOverview().isBlank()) {
            throw new IllegalArgumentException(
                    "overview는 필수입니다."
            );
        }
        if (request.getRoles() == null ||
                request.getRoles().isBlank()) {
            throw new IllegalArgumentException(
                    "roles는 필수입니다."
            );
        }
        if (request.getTechStack() == null ||
                request.getTechStack().isEmpty()) {
            throw new IllegalArgumentException(
                    "techStack은 필수입니다."
            );
        }
        if (request.getFeatures() == null ||
                request.getFeatures().isEmpty()) {
            throw new IllegalArgumentException(
                    "features는 필수입니다."
            );
        }
    }

    private PortfolioDetailResponse convertToResponse(
            PortfolioEntity portfolio
    ) {
        try {
            List<String> techStack =
                    objectMapper.readValue(
                            portfolio.getTechStackJson(),
                            new TypeReference<List<String>>() {}
                    );
            List<FeatureDTO> features =
                    objectMapper.readValue(
                            portfolio.getFeaturesJson(),
                            new TypeReference<List<FeatureDTO>>() {}
                    );
            List<TroubleshootDTO> troubleshoots =
                    objectMapper.readValue(
                            portfolio.getTroubleshootsJson(),
                            new TypeReference<List<TroubleshootDTO>>() {}
                    );
            PortfolioImageDTO images =
                    objectMapper.readValue(
                            portfolio.getImagesJson(),
                            PortfolioImageDTO.class
                    );
            return PortfolioDetailResponse.builder()
                    .id(portfolio.getId())
                    .projectId(portfolio.getProject().getId())
                    .projectName(portfolio.getProject().getTitle())
                    .overview(portfolio.getOverview())
                    .roles(portfolio.getRoles())
                    .techStack(techStack)
                    .features(features)
                    .troubleshoots(troubleshoots)
                    .metrics(portfolio.getMetrics())
                    .images(images)
                    .status(portfolio.getStatus())
                    .isPublic(false)
                    .shareToken(null)
                    .createdAt(portfolio.getCreatedAt())
                    .updatedAt(portfolio.getUpdatedAt())
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private PortfolioDetailResponse createInitialPortfolio(ProjectEntity project) {
        return PortfolioDetailResponse.builder()
                .id(null)
                .projectId(project.getId())
                .projectName(project.getTitle())
                .overview("")
                .roles("")
                .techStack(Arrays.stream(
                        project.getTechStack().split(",")
                ).map(String::trim).toList()
                )
                .features(List.of())
                .troubleshoots(List.of())
                .metrics("")
                .images(PortfolioImageDTO.builder()
                        .architecture(new ImageDTO(null, ""))
                        .erd(new ImageDTO(null, ""))
                        .ui(List.of())
                        .build()
                )
                .status("DRAFT")
                .isPublic(false)
                .shareToken(null)
                .build();
    }
}
