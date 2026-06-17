package com.devlog.devlog.auth.controller;

import com.devlog.devlog.auth.dto.portfolio.CreatePorfolioRequest;
import com.devlog.devlog.auth.dto.portfolio.PortfolioResponse;

import com.devlog.devlog.auth.service.PortfolioService;
import com.devlog.devlog.global.common.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
public class PortfolioController {
    private final PortfolioService portfolioService;
    @PostMapping
    public ResponseEntity<ApiResponse<PortfolioResponse>> createPortfolio(
            Authentication authentication,
            @RequestBody CreatePorfolioRequest request
            ) throws JsonProcessingException {
        PortfolioResponse res = portfolioService.createPortfolio(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("포트폴리오 저장 성공", res));
    }
}
