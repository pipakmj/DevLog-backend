package com.devlog.devlog.auth.dto.portfolio.response;

import com.devlog.devlog.auth.dto.portfolio.FeatureDTO;
import com.devlog.devlog.auth.dto.portfolio.PortfolioImageDTO;
import com.devlog.devlog.auth.dto.portfolio.TroubleshootDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioDetailResponse {
    private Long id;
    private Long projectId;
    private String projectName;
    private String overview;
    private String roles;
    private List<String> techStack;
    private List<FeatureDTO> features;
    private List<TroubleshootDTO> troubleshoots;
    private String metrics;
    private PortfolioImageDTO images;
    private String status;
    private Boolean isPublic;
    private String shareToken;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
