package com.devlog.devlog.auth.dto.portfolio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioResponse {
    private Long portfolioId;
    private String status;
    private LocalDateTime updatedAt;
}
