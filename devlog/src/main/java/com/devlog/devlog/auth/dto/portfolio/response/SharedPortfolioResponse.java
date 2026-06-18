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

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SharedPortfolioResponse {
    private Long id;
    private String projectName;
    private String overview;
    private String roles;
    private List<String> techStack;
    private List<FeatureDTO> features;
    private List<TroubleshootDTO> troubleshoots;
    private String metrics;
    private PortfolioImageDTO images;
    private LocalDateTime updatedAt;
}
