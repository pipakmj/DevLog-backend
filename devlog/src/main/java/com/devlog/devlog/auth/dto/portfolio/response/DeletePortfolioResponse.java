package com.devlog.devlog.auth.dto.portfolio.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeletePortfolioResponse {
    private Long portfolioId;
}
