package com.devlog.devlog.auth.controller;

import com.devlog.devlog.auth.dto.github.GitAnalyzeRequest;
import com.devlog.devlog.auth.dto.github.GitAnalyzeResponse;
import com.devlog.devlog.auth.service.GitAnalyzeService;
import com.devlog.devlog.global.common.ApiResponse;
import com.devlog.devlog.global.exception.BusinessException;
import com.devlog.devlog.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class GitAnalyzeController {

    private final GitAnalyzeService gitAnalyzeService;
    private final StringRedisTemplate redisTemplate;

    private static final String RATE_LIMIT_PREFIX = "analyze_limit:";
    private final int MAX_REQUEST_LIMIT = 5;

    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<GitAnalyzeResponse>> analyzeRepo(
            @RequestBody GitAnalyzeRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();
        String limitKey = RATE_LIMIT_PREFIX + userEmail;

        Long count = redisTemplate.opsForValue().increment(limitKey);

        if (count != null && count == 1L) {
            redisTemplate.expire(limitKey, Duration.ofDays(5));
        }

        if (count != null && count > MAX_REQUEST_LIMIT) {
            throw new BusinessException(ErrorCode.API_RATE_LIMIT_EXCEEDED);
        }

        GitAnalyzeResponse response =
                gitAnalyzeService.analyze(request.getGitUrl());

        int remaining = MAX_REQUEST_LIMIT - count.intValue();

        return ResponseEntity.ok()
                .header("X-RateLimit-Limit",
                        String.valueOf(MAX_REQUEST_LIMIT))
                .header("X-RateLimit-Remaining",
                        String.valueOf(remaining))
                .body(ApiResponse.success(
                        "프로젝트 분석이 완료 되었습니다.",
                        response));
    }
}
