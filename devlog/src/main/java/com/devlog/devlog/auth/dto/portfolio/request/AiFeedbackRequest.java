package com.devlog.devlog.auth.dto.portfolio.request;

import com.devlog.devlog.auth.dto.portfolio.FeatureDTO;
import com.devlog.devlog.auth.dto.portfolio.PortfolioImageDTO;
import com.devlog.devlog.auth.dto.portfolio.TroubleshootDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiFeedbackRequest {
    private Long projectId;
    private String overview;
    private String roles;
    private String projectPeriod;
    private String teamSize;
    private String primaryRole;
    private List<String> techStack;
    private List<FeatureDTO> features;
    private List<TroubleshootDTO> troubleshoots;
    private String metrics;
    private PortfolioImageDTO images;
}
