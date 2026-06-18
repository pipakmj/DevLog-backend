package com.devlog.devlog.auth.service;

import com.devlog.devlog.auth.dto.portfolio.request.*;
import com.devlog.devlog.auth.dto.portfolio.response.*;
import com.devlog.devlog.auth.entity.PortfolioEntity;
import com.devlog.devlog.auth.entity.ProjectEntity;
import com.devlog.devlog.auth.entity.UserEntity;
import com.devlog.devlog.auth.repository.PortfolioRepository;
import com.devlog.devlog.auth.repository.ProjectRepository;
import com.devlog.devlog.auth.repository.UserRepository;
import com.devlog.devlog.global.common.PdfGenerator;
import com.devlog.devlog.global.exception.BusinessException;
import com.devlog.devlog.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PortfolioService {
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final PortfolioRepository portfolioRepository;
    private final ObjectMapper objectMapper;
    private final PdfGenerator pdfGenerator;

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
    @Transactional
    public DeletePortfolioResponse deletePortfolio(String userEmail, Long portfolioId) {
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        PortfolioEntity portfolioEntity = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PORTFOLIO_NOT_FOUND));

        if(portfolioEntity.getProject().getUserEntity().getId() != user.getId()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_PORTFOLIO_ACCESS);
        }
        ProjectEntity project = portfolioEntity.getProject();
        /*
         * 양방향 OneToOne 관계 해제
         *
         * ProjectEntity -> PortfolioEntity 참조가 남아있는 상태에서
         * PortfolioEntity를 삭제하면 Hibernate flush 과정에서
         * TransientObjectException이 발생할 수 있음.
         *
         * 추후 구조 개선 시:
         * 1. Portfolio를 독립 엔티티로 관리하거나
         * 2. orphanRemoval/cascade 정책 재검토 필요
         */
        project.setPortfolioEntity(null);
        // 변경된 연관관계 반영
        projectRepository.save(project);
        /*
         * 포트폴리오 삭제
         *
         * 현재 OneToOne 양방향 관계로 인해
         * 관계 해제 후 삭제를 수행한다.
         */
        portfolioRepository.delete(portfolioEntity);
        return DeletePortfolioResponse.builder()
                .portfolioId(portfolioId)
                .build();
    }

    public PdfDownloadResponse generatePdf(
            String userEmail,
            Long portfolioId,
            PortfolioPdfRequest request
    ) throws JsonProcessingException {
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        PortfolioEntity portfolioEntity = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PORTFOLIO_NOT_FOUND));

        if(portfolioEntity.getProject().getUserEntity().getId() != user.getId()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_PORTFOLIO_ACCESS);
        }
        byte[] pdfBytes = pdfGenerator.generatePdf(portfolioEntity);

        return PdfDownloadResponse.builder()
                .fileName(request.getFileName())
                .pdfBytes(pdfBytes)
                .build();
    }

    @Transactional
    public SharePortfolioResponse sharePortfolio(
            String userEmail,
            Long portfolioId,
            SharePortfolioRequest request
    ){
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        PortfolioEntity portfolioEntity = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PORTFOLIO_NOT_FOUND));

        if(portfolioEntity.getProject().getUserEntity().getId() != user.getId()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_PORTFOLIO_ACCESS);
        }
        portfolioEntity.setPublic(request.isPublic());
        if (Boolean.TRUE.equals(request.isPublic())) {
            if(portfolioEntity.getShareToken() == null) {
                portfolioEntity.setShareToken(
                        "pf_" + UUID.randomUUID()
                                .toString()
                                .replace("-", "")
                                .substring(0, 8)
                );
            }
        } else {
            portfolioEntity.setShareToken(null);
        }
        portfolioRepository.save(portfolioEntity);
        String shareUrl = portfolioEntity.getShareToken() == null
                ? null
                : "http://localhost:5173/portfolio/share/"
                + portfolioEntity.getShareToken();
        return SharePortfolioResponse.builder()
                .portfolioId(portfolioId)
                .isPublic(request.isPublic())
                .shareToken(portfolioEntity.getShareToken())
                .shareUrl(shareUrl)
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
