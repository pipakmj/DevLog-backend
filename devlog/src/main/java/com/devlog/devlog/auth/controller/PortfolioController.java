package com.devlog.devlog.auth.controller;

import com.devlog.devlog.auth.dto.portfolio.request.CreatePortfolioRequest;
import com.devlog.devlog.auth.dto.portfolio.request.PortfolioPdfRequest;
import com.devlog.devlog.auth.dto.portfolio.request.SharePortfolioRequest;
import com.devlog.devlog.auth.dto.portfolio.response.*;

import com.devlog.devlog.auth.service.PortfolioService;
import com.devlog.devlog.global.common.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PortfolioController {
    private final PortfolioService portfolioService;
    @PostMapping("/portfolios")
    public ResponseEntity<ApiResponse<PortfolioResponse>> createPortfolio(
            Authentication authentication,
            @RequestBody CreatePortfolioRequest request
            ) throws JsonProcessingException {
        PortfolioResponse res = portfolioService.createPortfolio(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("포트폴리오 저장 성공", res));
    }
    @GetMapping("/portfolios/{portfolioId}")
    public ResponseEntity<ApiResponse<PortfolioDetailResponse>> getPortfolio(
            Authentication authentication,
            @PathVariable Long portfolioId
    ) {
        PortfolioDetailResponse res = portfolioService.getPortfolio(authentication.getName(), portfolioId);
        return ResponseEntity.ok(ApiResponse.success("포트폴리오 조회 성공", res));
    }
    @GetMapping("/projects/{projectId}/portfolio")
    public ResponseEntity<ApiResponse<ProjectPortfolioResponse>> getProjectPortfolio(
            Authentication authentication,
            @PathVariable Long projectId
    ){
        ProjectPortfolioResponse res = portfolioService.getProjectPortfolio(authentication.getName(), projectId);
        return ResponseEntity.ok(ApiResponse.success("프로젝트 포트폴리오 조회 성공", res));
    }
    @PatchMapping("/portfolios/{portfolioID}")
    public ResponseEntity<ApiResponse<PortfolioResponse>> updatePortfolio(
            Authentication authentication,
            @PathVariable Long portfolioID,
            @RequestBody CreatePortfolioRequest request
    ) throws JsonProcessingException {
        PortfolioResponse res = portfolioService.updatePortfolio(authentication.getName(), portfolioID, request);
        return ResponseEntity.ok(ApiResponse.success("포트폴리오 수정 성공", res));
    }
    @DeleteMapping("/portfolios/{portfolioId}")
    public ResponseEntity<ApiResponse<DeletePortfolioResponse>> deletePortfolio(
            Authentication authentication,
            @PathVariable Long portfolioId
    ){
        DeletePortfolioResponse res = portfolioService.deletePortfolio(authentication.getName(), portfolioId);
        return ResponseEntity.ok(ApiResponse.success("포트폴리오 삭제 성공", res));
    }
    @PostMapping(
            value = "/portfolios/{portfolioId}/pdf",
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<byte[]> downloadPdf(
            Authentication authentication,
            @PathVariable Long portfolioId,
            @RequestBody PortfolioPdfRequest request
    ) throws JsonProcessingException {
        PdfDownloadResponse pdf =
                portfolioService.generatePdf(
                        authentication.getName(),
                        portfolioId,
                        request
                );
        return ResponseEntity.ok().header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + pdf.getFileName() + "\""
        ).contentType(MediaType.APPLICATION_PDF).body(pdf.getPdfBytes());
    }
    @PostMapping("/portfolios/{portfolioId}/share")
    public ResponseEntity<ApiResponse<SharePortfolioResponse>> sharePortfolio(
            Authentication authentication,
            @PathVariable Long portfolioId,
            @RequestBody SharePortfolioRequest request
    ){
        SharePortfolioResponse res = portfolioService.sharePortfolio(authentication.getName(), portfolioId, request);
        return ResponseEntity.ok(ApiResponse.success("공유 링크 생성 성공", res));
    }
}
