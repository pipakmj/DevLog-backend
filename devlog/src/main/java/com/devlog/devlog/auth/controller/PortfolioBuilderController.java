package com.devlog.devlog.auth.controller;

import com.devlog.devlog.auth.dto.portfolio.PortfolioBuilderProjectResponse;
import com.devlog.devlog.auth.service.PortfolioBuilderService;
import com.devlog.devlog.global.common.ApiResponse;
import com.devlog.devlog.global.common.CustomPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolio-builder")
@RequiredArgsConstructor
public class PortfolioBuilderController {
    private final PortfolioBuilderService portfolioBuilderService;

    @GetMapping("/projects")
    public ResponseEntity<ApiResponse<CustomPageResponse<PortfolioBuilderProjectResponse>>> getProjects(
            Authentication authentication,
            @PageableDefault(page = 0, size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("프로젝트 목록 조회 성공",
                portfolioBuilderService.getPortfolioBuilderProjects(authentication.getName(), pageable)));
    }
}