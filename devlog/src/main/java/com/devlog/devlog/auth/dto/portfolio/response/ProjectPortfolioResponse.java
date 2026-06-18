package com.devlog.devlog.auth.dto.portfolio.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectPortfolioResponse {
    private boolean exists;
    private PortfolioDetailResponse portfolio;
}
