package com.devlog.devlog.auth.dto.portfolio.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharePortfolioResponse {
    private Long portfolioId;
    @JsonProperty("isPublic")
    private boolean isPublic;
    private String shareToken;
    private String shareUrl;
}
