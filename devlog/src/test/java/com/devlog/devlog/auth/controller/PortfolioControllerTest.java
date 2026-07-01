package com.devlog.devlog.auth.controller;

import com.devlog.devlog.auth.dto.portfolio.request.AiFeedbackRequest;
import com.devlog.devlog.auth.service.PortfolioService;
import com.devlog.devlog.global.common.RateLimitService;
import com.devlog.devlog.global.common.UsageLimitResponse;
import com.devlog.devlog.global.config.SecurityConfig;
import com.devlog.devlog.global.provider.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PortfolioController.class)
@Import(SecurityConfig.class)
@org.springframework.test.context.ActiveProfiles("test")
@DisplayName("PortfolioController AI 피드백 롤백 테스트")
public class PortfolioControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private PortfolioService portfolioService;
    @MockitoBean
    private RateLimitService rateLimitService;
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser(username = "user@test.com")
    @DisplayName("실패: 포트폴리오 AI 진단 중 에러가 발생해 실패하면, 차단 횟수 롤백이 반드시 수행된다.")
    void createAiFeedback_Fail_RollbackExecuted() throws Exception {
        UsageLimitResponse usageLimitResponse = UsageLimitResponse.builder()
                .dailyLimit(5)
                .used(1)
                .remaining(4)
                .build();
        when(rateLimitService.checkAndIncrement(any(),any(), anyInt(), any())).thenReturn(usageLimitResponse);
        when(portfolioService.createAiFeedback(eq("user@test.com"), any(AiFeedbackRequest.class))).thenThrow(new RuntimeException("Gemini API Timeout"));

        mockMvc.perform(post("/api/portfolios/ai-feedback")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"projectId\":1, \"readme\":\"# Project\", \"commits\":[]}"))
                .andExpect(status().isInternalServerError());

        verify(rateLimitService, times(1)).rollback(anyString(), eq("user@test.com"));
    }
}
