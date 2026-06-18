package com.devlog.devlog.auth.dto.portfolio.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SharePortfolioRequest {
    @JsonProperty("isPublic")
    private boolean isPublic;
}
