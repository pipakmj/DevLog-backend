package com.devlog.devlog.auth.dto.portfolio;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePorfolioRequest {
    private Long projectId;
    private String overview;
    private String roles;
    private List<String> techStack;
    private List<FeatureRequest> features;
    private List<TroubleshootRequest> troubleshoots;
    private String metrics;
    private PortfolioImageRequest images;
    private String status;
}
