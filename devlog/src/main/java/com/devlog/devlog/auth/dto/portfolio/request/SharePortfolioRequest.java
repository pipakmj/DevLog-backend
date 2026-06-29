package com.devlog.devlog.auth.dto.portfolio.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SharePortfolioRequest {
    @JsonProperty("isPublic")
    private boolean isPublic;
}
